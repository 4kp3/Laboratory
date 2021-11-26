node {
    def projectName = "Laboratory"
    def result

    try {

        stage('Config') {

            //因开了梯子，需要对git设置好代理
            //sh 'git config --add remote.origin.proxy "http://127.0.0.1:7890"'
            //sh "git config --local --list "

            checkout poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], gitTool: 'git',
            userRemoteConfigs: [[credentialsId: 'c8bbab0a-8b1f-4bbe-9e7c-e03a83db5212', url: 'https://github.com/4kp3/Laboratory.git']]]
        }

        stage('Build') {
            sh "./gradlew :app:assemble"
        }

        stage("Archive"){
            archiveArtifacts artifacts: "app/build/outputs/apk/debug/*.apk", followSymlinks: false
        }

        result = true
    } catch (e) {
        echo 'run with exception'
        result = false
        // Since we're catching the exception in order to report on it,
        // we need to re-throw it, to ensure that the build is marked as failed
        throw e
    } finally {
        def currentResult = currentBuild.result ?: 'NONE'
        echo "构建结果：${currentResult}"

        mail bcc: '', body: "构建结果：${currentResult}", cc: '', charset: 'utf-8', from: 'shipofimagination@yeah.net',
        mimeType: 'text/plain',
        replyTo: 'Jenkins Auto Reploy',
        subject: 'Hello Jenkins Build Result',
        to: 'mostwantx@163.com'

        //ding notify
        //dingtalk robot: "c39d892c-c631-4de4-9ce6-2d93c65da5ad",type:"TEXT",text:["描述1","描述2","状态：${result}"],at:["18676668749"]
    }
}
