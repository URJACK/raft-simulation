# 事件驱动仿真模拟器

## 第一章·前言

### 网络仿真基础包

该项目，使用java编写，属于事件驱动类型的网络仿真模拟器。

`com.sicnu.netsimu` 是我的模拟器包。

`com.sicnu.netsimu.core` 是我事件驱动仿真的核心部分。

`com.sicnu.netsimu.ui` 则是与`core`包内的代码合作，控制core包中内容的输出形式。

使用`core包`与`ui包`，就可以完成并搭建**网络仿真环境**了。

如果你不做Raft的相关实验，只需要掌握前面这两个包的使用方法就行了。

但是具体如何使用这两个包，可以参照 `raft包` 是如何使用这两个包的。

### Raft仿真算法包

因原定名称是`RaftSimulation`，所以我编写了raft包，
用户可以使用其中的`RaftMote`来<u>进行Raft的相关实验</u>。

raft包使用 `com.sicnu.raft` ，也就是核心部分作为依赖，并进行了如下操作：

1. 编写 `com.sicnu.raft.command` ：

   你需要编写新的命令，命令的编写方法。每个命令必须传入三个基础属性： `simulator、timestamp、type`

   其他的属性可以自行定义。

   ```java
   public class RaftBeatCommand extends Command{
       public RaftBeatCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
           super(simulator, timeStamp, type);
           //...
       }
   }
   ```

   其次编写了相应的`CommandTranslator`

   在`extendInit` 和 `extendParse` 中，进行了命令的解析操作。

   ```java
   public class RaftCommandTranslator extends CommandTranslator {
       protected HashSet<String> extendsCommandTypeHashset;
       protected HashMap<String, Integer> extendsCommandLengthHashMap;
       protected HashMap<String, RaftOpCommand.Operation> raftOperation;
   }
   ```

2. 编写了 `com.sicnu.raft.mote` 中的 `RaftMote` 

   ```java
   public class RaftMote extends Mote {
       // ...
       public RaftMote(NetSimulator simulator, int moteId, float x, float y, String... args) {
           super(simulator, moteId, x, y, RaftMote.class);
           // ...
       }
       // ... 
   }
   ```

   `RaftMote`去继承`Mote`就可以同样具备在仿真网络中的运行的能力。

需要注意的是，这里只是简单说明了一下，至少应该编写哪两个包，才能进行实验，
但是并没有对具体的编程方法作出解释。
具体的编写方法和细节请参考<u>第二章：编写方法</u>。

## 第二章·编写方法

### 编写节点逻辑--`Mote`类



### 编写命令--`Command`类 & `CommandTranslator`类

