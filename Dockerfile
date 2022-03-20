# 该镜像需要依赖的基础镜像
FROM adoptopenjdk/openjdk8
# 指定维护者的名字
MAINTAINER wayn111
WORKDIR /root/workspace
# 将当前目录下的jar包复制到docker容器的/目录下
ADD target/newbee-mall-2.1.2.jar /opt/newbeemall/newbee-mall-2.1.2.jar
# 运行过程中创建一个mall-tiny-docker-file.jar文件
RUN bash -c 'touch /opt/newbeemall/newbee-mall-2.1.2.jar'
# 声明服务运行在8000端口
EXPOSE 84
# 指定docker容器启动时运行jar包
ENTRYPOINT ["sh", "-c", "exec java -jar -Xms1024m -Xmx1025m /opt/newbeemall/newbee-mall-2.1.2.jar"]
