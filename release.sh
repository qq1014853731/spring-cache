# 1. 将版本修改为 SNAPSHOT （-SNAPSHOT版本可修改，可重复提交）
# 2. mvn clean deploy -P release -Dmaven.test.skip=true

# 3. 将版本修改为 RELEASE 或 空 （RELEASE版本只允许发布一次）
# 4. mvn clean deploy -P release -Dmaven.test.skip=true
# 5. mvn -P release org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:release