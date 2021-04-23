<p>
	<h1 align="center">DingTalkRobotUtils</h1>
</p>
<p>
	<h2 align="center">钉钉机器人发送工具类</h2>
</p>

<p align="center">
	<img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/AochongZhang/DingTalkRobotUtils">
	<img alt="GitHub" src="https://img.shields.io/github/license/AochongZhang/DingTalkRobotUtils">
</p>

### 特性

+ 支持所有消息类型，支持加签
+ 依赖少，无需引入特殊依赖
+ 单文件，使用方便快捷
+ 支持发送响应结果检测

### 依赖

+ jdk1.8 (其他版本未测试)
+ spring5 (其他版本未测试)
+ apache commons codec (不需要加签方式发送消息无需依赖，注释buildSignUrl方法即可)

### 使用

#### 发送文本消息

```java
DingTalkRobotUtils.sendText(钉钉机器人地址, 消息内容);
DingTalkRobotUtils.sendText(钉钉机器人地址, 消息内容, 是否@所有人);
DingTalkRobotUtils.sendText(钉钉机器人地址, 消息内容, 是否@所有人, 被@人的手机号集合);
```

#### 发送链接消息

```java
DingTalkRobotUtils.sendLink(钉钉机器人地址, 消息标题, 消息内容, 点击消息跳转的URL);
DingTalkRobotUtils.sendLink(钉钉机器人地址, 消息标题, 消息内容, 点击消息跳转的URL, 图片URL);
```

#### 发送Markdown消息

```java
DingTalkRobotUtils.sendMarkdown(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息);
DingTalkRobotUtils.sendMarkdown(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 是否@所有人);
DingTalkRobotUtils.sendMarkdown(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 是否@所有人, 被@人的手机号在text内容里要有@人的手机号);
```

#### 发送整体跳转ActionCard消息

```java
DingTalkRobotUtils.sendActionCard(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 单个按钮的标题, 点击singleTitle按钮触发的URL);
DingTalkRobotUtils.sendActionCard(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 单个按钮的标题, 点击singleTitle按钮触发的URL, 按钮排列方式);
```

#### 发送独立跳转ActionCard消息

```java
DingTalkRobotUtils.sendActionCard(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 按钮集合);
DingTalkRobotUtils.sendActionCard(钉钉机器人地址, 首屏会话透出的展示内容, markdown格式的消息, 按钮集合, 按钮排列方式);
```

#### 发送FeedCard消息

```java
DingTalkRobotUtils.sendFeedCard(钉钉机器人地址, 链接集合);
```

#### 发送自行创建的消息对象

```java
DingTalkRobotUtils.send(钉钉机器人地址, 消息对象);
```

#### 构建加签URL

```java
// 将加签后的url传入发送方法即可
String url = DingTalkRobotUtils.buildSignUrl(钉钉机器人地址, 密钥)
```

#### 检测发送响应结果

检测到发送结果错误将抛出DingTalkRobotException异常

```java
DingTalkResponse response = DingTalkRobotUtils.sendText(钉钉机器人地址, 消息内容);
DingTalkRobotUtils.checkResponse(response);
// 或
DingTalkRobotUtils.sendText(钉钉机器人地址, 消息内容).check();
```

更多详细内容可参考代码注释和钉钉机器人官方文档

### Markdown

支持的语法

```markdown
# 一级标题
## 二级标题
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题

> 引用

**加粗**
*斜体*

[链接](https://github.com)

图片
![](https://github.com)

无序列表
- item1
- item2

有序列表
1. item1
2. item2

font标签
<font color=#ff0000 size=6 face="黑体">内容</font> 
```

