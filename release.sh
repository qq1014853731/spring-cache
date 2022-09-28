# 1. 将版本修改为 SNAPSHOT （-SNAPSHOT版本可修改，可重复提交）
# 2. mvn clean deploy -P release -D maven.test.skip=true

# 3. 将版本修改为 RELEASE 或 空 （RELEASE版本只允许发布一次）
# 4. mvn clean deploy -P release -D maven.test.skip=true
# 如果下面这条命令发布失败，请检查是否已经发布到了仓库，注意：是否开启了自动发布（autoReleaseAfterClose）
# 5. mvn -P release org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:release