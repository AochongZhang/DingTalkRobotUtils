import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

/**
 * 钉钉机器人发送工具类
 *
 * 特性:
 *     支持所有消息类型
 *     依赖少
 *     单文件，使用方便
 *
 * 依赖:
 *     jdk1.8 (其他版本未测试)
 *     spring5 (其他版本未测试)
 *     apache commons codec (不需要加签方式发送消息无需依赖，注释{@link #buildSignUrl}方法即可)
 *
 * @author Aochong Zhang
 * @version 1.0
 * @date 2021-04-23 09:10
 */
public abstract class DingTalkRobotUtils {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final HttpHeaders HEADERS = new HttpHeaders();
    /** 消息类型 */
    private static final String MSG_TYPE_TEXT = "text";
    private static final String MSG_TYPE_LINK = "link";
    private static final String MSG_TYPE_MARKDOWN = "markdown";
    private static final String MSG_TYPE_ACTION_CARD = "actionCard";
    private static final String MSG_TYPE_FEED_CARD = "feedCard";

    static {
        HEADERS.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 发送文本消息
     *
     * @param url 钉钉机器人地址
     * @param content 消息内容
     * @return 请求响应
     */
    public static DingTalkResponse sendText(String url, String content) {
        return sendText(url, content, false);
    }

    /**
     * 发送文本消息
     *
     * @param url 钉钉机器人地址
     * @param content 消息内容
     * @param isAtAll 是否@所有人
     * @return 请求响应
     */
    public static DingTalkResponse sendText(String url, String content, boolean isAtAll) {
        return sendText(url, content, isAtAll, null);
    }


    /**
     * 发送文本消息
     *
     * @param url 钉钉机器人地址
     * @param content 消息内容
     * @param isAtAll 是否@所有人
     * @param atMobiles 被@人的手机号
     * @return 请求响应
     */
    public static DingTalkResponse sendText(String url, String content, boolean isAtAll, Set<String> atMobiles) {
        DingTalkRequestText request = new DingTalkRequestText();
        DingTalkRequestText.Text text = new DingTalkRequestText.Text(content);
        request.setText(text);
        At at = new At();
        at.setIsAtAll(isAtAll);
        at.setAtMobiles(atMobiles);
        request.setAt(at);
        return send(url, request);
    }

    /**
     * 发送链接消息
     *
     * @param url 钉钉机器人地址
     * @param title 消息标题
     * @param text 消息内容，如果太长只会部分展示
     * @param messageUrl 点击消息跳转的URL
     * @return 请求响应
     */
    public static DingTalkResponse sendLink(String url, String title, String text, String messageUrl) {
        return sendLink(url, title, text, messageUrl, "");
    }

    /**
     * 发送链接消息
     *
     * @param url 钉钉机器人地址
     * @param title 消息标题
     * @param text 消息内容，如果太长只会部分展示
     * @param messageUrl 点击消息跳转的URL
     * @param picUrl 图片URL
     * @return 请求响应
     */
    public static DingTalkResponse sendLink(String url, String title, String text, String messageUrl, String picUrl) {
        DingTalkRequestLink request = new DingTalkRequestLink();
        DingTalkRequestLink.Link link = new DingTalkRequestLink.Link();
        link.setTitle(title);
        link.setText(text);
        link.setMessageUrl(messageUrl);
        link.setPicUrl(picUrl);
        request.setLink(link);
        return send(url, request);
    }

    /**
     * 发送Markdown消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @return 请求响应
     */
    public static DingTalkResponse sendMarkdown(String url, String title, String text) {
        return sendMarkdown(url, title, text, false);
    }

    /**
     * 发送Markdown消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param isAtAll 是否@所有人
     * @return 请求响应
     */
    public static DingTalkResponse sendMarkdown(String url, String title, String text, boolean isAtAll) {
        return sendMarkdown(url, title, text, isAtAll, null);
    }

    /**
     * 发送Markdown消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param isAtAll 是否@所有人
     * @param atMobiles 被@人的手机号, 在text内容里要有@人的手机号
     * @return 请求响应
     */
    public static DingTalkResponse sendMarkdown(String url, String title, String text, boolean isAtAll, Set<String> atMobiles) {
        DingTalkRequestMarkdown request = new DingTalkRequestMarkdown();
        DingTalkRequestMarkdown.Markdown markdown = new DingTalkRequestMarkdown.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        request.setMarkdown(markdown);
        At at = new At();
        at.setIsAtAll(isAtAll);
        at.setAtMobiles(atMobiles);
        request.setAt(at);
        return send(url, request);
    }

    /**
     * 发送整体跳转ActionCard类型消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param singleTitle 单个按钮的标题
     * @param singleURL 点击singleTitle按钮触发的URL, 设置此项和singleURL后，btns无效
     * @return 请求响应
     */
    public static DingTalkResponse sendActionCard(String url, String title, String text, String singleTitle, String singleURL) {
        return sendActionCard(url, title, text, singleTitle, singleURL, null);
    }

    /**
     * 发送整体跳转ActionCard类型消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param singleTitle 单个按钮的标题
     * @param singleURL 点击singleTitle按钮触发的URL, 设置此项和singleURL后，btns无效
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @return 请求响应
     */
    public static DingTalkResponse sendActionCard(String url, String title, String text, String singleTitle, String singleURL, String btnOrientation) {
        DingTalkRequestActionCard request = new DingTalkRequestActionCard();
        DingTalkRequestActionCard.ActionCard actionCard = new DingTalkRequestActionCard.ActionCard();
        actionCard.setTitle(title);
        actionCard.setText(text);
        actionCard.setSingleTitle(singleTitle);
        actionCard.setSingleURL(singleURL);
        actionCard.setBtnOrientation(btnOrientation);
        request.setActionCard(actionCard);
        return send(url, request);
    }

    /**
     * 发送独立跳转ActionCard类型消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param btns 按钮
     * @return 请求响应
     */
    public static DingTalkResponse sendActionCard(String url, String title, String text, List<DingTalkRequestActionCard.ActionCard.Btn> btns) {
        return sendActionCard(url, title, text, btns, null);
    }

    /**
     * 发送独立跳转ActionCard类型消息
     *
     * @param url 钉钉机器人地址
     * @param title 首屏会话透出的展示内容
     * @param text markdown格式的消息
     * @param btns 按钮
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @return 请求响应
     */
    public static DingTalkResponse sendActionCard(String url, String title, String text, List<DingTalkRequestActionCard.ActionCard.Btn> btns, String btnOrientation) {
        DingTalkRequestActionCard request = new DingTalkRequestActionCard();
        DingTalkRequestActionCard.ActionCard actionCard = new DingTalkRequestActionCard.ActionCard();
        actionCard.setTitle(title);
        actionCard.setText(text);
        actionCard.setBtns(btns);
        actionCard.setBtnOrientation(btnOrientation);
        request.setActionCard(actionCard);
        return send(url, request);
    }

    /**
     * 发送FeedCard类型消息
     *
     * @param url 钉钉机器人地址
     * @param links 链接
     * @return 请求响应
     */
    public static DingTalkResponse sendFeedCard(String url, List<DingTalkRequestFeedCard.Link> links) {
        DingTalkRequestFeedCard request = new DingTalkRequestFeedCard();
        DingTalkRequestFeedCard.FeedCard feedCard = new DingTalkRequestFeedCard.FeedCard();
        feedCard.setLinks(links);
        request.setFeedCard(feedCard);
        return send(url, request);
    }

    /**
     * 发送钉钉机器人消息
     *
     * @param url 钉钉机器人地址
     * @param dingTalkRequest 请求数据
     * @return 请求响应
     */
    public static DingTalkResponse send(String url, DingTalkRequest dingTalkRequest) {
        return REST_TEMPLATE.postForObject(url, new HttpEntity<>(dingTalkRequest, HEADERS), DingTalkResponse.class);
    }

    /**
     * 构建加签url
     *
     * @param url 钉钉机器人地址
     * @param secret 密钥
     * @return 请求响应
     */
    public static String buildSignUrl(String url, String secret) {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
            return url + "&timestamp=" + timestamp + "&sign=" + sign;
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 响应结果检测
     *
     * @param response 请求响应
     * @see DingTalkRobotException
     */
    public static void checkResponse(DingTalkResponse response) {
        if (response != null && "0".equals(response.getErrcode())) {
            return;
        }
        throw new DingTalkRobotException(response);
    }


    /**
     * 钉钉机器人请求体基础类
     */
    public abstract static class DingTalkRequest {
        /** 消息类型 */
        private final String msgtype;
        private At at = new At();

        public DingTalkRequest(String msgtype) {
            this.msgtype = msgtype;
        }

        public String getMsgtype() {
            return msgtype;
        }

        public At getAt() {
            return at;
        }

        public void setAt(At at) {
            this.at = at;
        }
    }

    public static class At {
        /** 被@人的手机号 */
        private Set<String> atMobiles;
        /** 被@人的用户userid */
        private Set<String> atUserIds;
        /** 是否@所有人 */
        private Boolean isAtAll = false;

        public Set<String> getAtMobiles() {
            return atMobiles;
        }

        public void setAtMobiles(Set<String> atMobiles) {
            this.atMobiles = atMobiles;
        }

        public Set<String> getAtUserIds() {
            return atUserIds;
        }

        public void setAtUserIds(Set<String> atUserIds) {
            this.atUserIds = atUserIds;
        }

        public Boolean getIsAtAll() {
            return isAtAll;
        }

        public void setIsAtAll(Boolean atAll) {
            isAtAll = atAll;
        }
    }

    /**
     * text类型消息
     */
    public static class DingTalkRequestText extends DingTalkRequest {
        private Text text;

        public DingTalkRequestText() {
            super(MSG_TYPE_TEXT);
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }

        public static class Text {
            /** 消息内容 */
            private String content;

            public Text(String content) {
                this.content = content;
            }

            public String getContent() {
                return content;
            }
        }
    }

    /**
     * link类型消息
     */
    public static class DingTalkRequestLink extends DingTalkRequest {
        private Link link;

        public DingTalkRequestLink() {
            super(MSG_TYPE_LINK);
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public static class Link {
            /** 消息标题 */
            private String title;
            /** 消息内容。如果太长只会部分展示 */
            private String text;
            /** 点击消息跳转的URL */
            private String messageUrl;
            /** 图片URL */
            private String picUrl;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getMessageUrl() {
                return messageUrl;
            }

            public void setMessageUrl(String messageUrl) {
                this.messageUrl = messageUrl;
            }

            public String getPicUrl() {
                return picUrl;
            }

            public void setPicUrl(String picUrl) {
                this.picUrl = picUrl;
            }
        }
    }

    /**
     * markdown类型消息
     */
    public static class DingTalkRequestMarkdown extends DingTalkRequest {
        private Markdown markdown;

        public DingTalkRequestMarkdown() {
            super(MSG_TYPE_MARKDOWN);
        }

        public Markdown getMarkdown() {
            return markdown;
        }

        public void setMarkdown(Markdown markdown) {
            this.markdown = markdown;
        }

        public static class Markdown {
            /** 首屏会话透出的展示内容 */
            private String title;
            /** markdown格式的消息 */
            private String text;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }

    /**
     * 整体跳转ActionCard类型消息
     */
    public static class DingTalkRequestActionCard extends DingTalkRequest {
        private ActionCard actionCard;

        public ActionCard getActionCard() {
            return actionCard;
        }

        public void setActionCard(ActionCard actionCard) {
            this.actionCard = actionCard;
        }

        public DingTalkRequestActionCard() {
            super(MSG_TYPE_ACTION_CARD);
        }

        public static class ActionCard {
            /** 首屏会话透出的展示内容 */
            private String title;
            /** markdown格式的消息 */
            private String text;
            /** 单个按钮的标题 设置此项和singleURL后，btns无效*/
            private String singleTitle;
            /** 点击singleTitle按钮触发的URL */
            private String singleURL;
            /** 0：按钮竖直排列 1：按钮横向排列 */
            private String btnOrientation;
            private String hideAvatar;
            /** 按钮 */
            private List<Btn> btns;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getSingleTitle() {
                return singleTitle;
            }

            public void setSingleTitle(String singleTitle) {
                this.singleTitle = singleTitle;
            }

            public String getSingleURL() {
                return singleURL;
            }

            public void setSingleURL(String singleURL) {
                this.singleURL = singleURL;
            }

            public String getBtnOrientation() {
                return btnOrientation;
            }

            public void setBtnOrientation(String btnOrientation) {
                this.btnOrientation = btnOrientation;
            }

            public String getHideAvatar() {
                return hideAvatar;
            }

            public void setHideAvatar(String hideAvatar) {
                this.hideAvatar = hideAvatar;
            }

            public List<Btn> getBtns() {
                return btns;
            }

            public void setBtns(List<Btn> btns) {
                this.btns = btns;
            }

            public static class Btn {
                /** 按钮标题 */
                private String title;
                /** 点击按钮触发的URL */
                private String actionURL;

                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public String getActionURL() {
                    return actionURL;
                }

                public void setActionURL(String actionURL) {
                    this.actionURL = actionURL;
                }
            }
        }
    }

    /**
     * FeedCard类型消息
     */
    public static class DingTalkRequestFeedCard extends DingTalkRequest {
        private FeedCard feedCard;

        public FeedCard getFeedCard() {
            return feedCard;
        }

        public void setFeedCard(FeedCard feedCard) {
            this.feedCard = feedCard;
        }

        public DingTalkRequestFeedCard() {
            super(MSG_TYPE_FEED_CARD);
        }

        public static class FeedCard {
            private List<Link> links;

            public List<Link> getLinks() {
                return links;
            }

            public void setLinks(List<Link> links) {
                this.links = links;
            }
        }

        public static class Link {
            /** 消息标题 */
            private String title;
            /** 消息内容。如果太长只会部分展示 */
            private String text;
            /** 点击消息跳转的URL */
            private String messageURL;
            /** 图片URL */
            private String picURL;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getMessageURL() {
                return messageURL;
            }

            public void setMessageURL(String messageURL) {
                this.messageURL = messageURL;
            }

            public String getPicURL() {
                return picURL;
            }

            public void setPicURL(String picURL) {
                this.picURL = picURL;
            }
        }
    }

    /**
     * 钉钉机器人响应
     */
    public static class DingTalkResponse {
        private String errcode;
        private String errmsg;

        public void check() {
            checkResponse(this);
        }

        public String getErrcode() {
            return errcode;
        }

        public void setErrcode(String errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }

        @Override
        public String toString() {
            return "DingTalkResponse{" +
                    "errcode='" + errcode + '\'' +
                    ", errmsg='" + errmsg + '\'' +
                    '}';
        }
    }

    public static class DingTalkRobotException extends RuntimeException {
        public DingTalkRobotException(DingTalkResponse response) {
            super(response == null ? "响应消息为空" : "errcode=" + response.getErrcode() + " errmsg=" + response.getErrmsg());
        }
    }
}
