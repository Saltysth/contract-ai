#!/bin/bash



# 指定查找端口
PORT=13000
# 指定要启动的程序路径
JAR_PATH="/data/jars/contract-ai.jar"
LOG_PATH="/data/jars/logs/contract-ai.log"

# 获取指定端口的 PID
PID=$(lsof -ti:$PORT)


# 判断是否找到了进程
if [ -n "$PID" ]; then
  echo "正在杀死 PID: $PID"
  # 杀死进程
  kill -9 $PID
  echo "进程 $PID 已被杀死"
else
 echo "没有找到占用端口 $PORT 的进程"
fi
sleep 1

# 启动新的 Java 程序
echo "启动新的程序..."
nohup java  -jar $JAR_PATH > $LOG_PATH 2>&1 &
echo "日志位于: $LOG_PATH"
sleep 1
echo "程序已启动"
