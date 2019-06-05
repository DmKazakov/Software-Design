package ru.hse.spb.kazakov

import com.amazonaws.SdkClientException
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.*

fun main(args: Array<String>) {
    System.setProperty("aws.accessKeyId", "accessKey")
    System.setProperty("aws.secretKey", "secretKey")

    Thread.sleep(10000)
    if (args.isEmpty()) {
        startQueues()
    } else if (args.size == 2) {
        Thread.sleep(25000)
        resendQueueMessages(args[0], args[1])
    }
}

fun resendQueueMessages(prodQueueName: String, consQueueName: String) {
    try {
        val sqs = AmazonSQSClientBuilder.standard()
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration("http://localstack:4576", "eu-west-1")
            )
            .build()

        val prodQueueUrl = sqs.getQueueUrl(GetQueueUrlRequest(prodQueueName)).queueUrl
        val consQueueUrl = sqs.getQueueUrl(GetQueueUrlRequest(consQueueName)).queueUrl

        while (true) {
            sqs.receiveMessage(ReceiveMessageRequest(prodQueueUrl)).messages.forEach {
                val num = it.body.toInt()
                println(num)
                sqs.deleteMessage(DeleteMessageRequest(prodQueueUrl, it.receiptHandle))
                sqs.sendMessage(SendMessageRequest(consQueueUrl, (num + 1).toString()))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Thread.sleep(1)
    }
}

fun startQueues() {
    val queueUrl = createQueue("A").queueUrl
    createQueue("B")
    sendMessage(queueUrl, "1")
}

private fun createQueue(queueName: String): CreateQueueResult {
    while (true) {
        try {
            val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localstack:4576", "eu-west-1")
                )
                .build()
            val createQueueRequestA = CreateQueueRequest(queueName)
            return sqs.createQueue(createQueueRequestA)
        } catch (e: SdkClientException) {
            e.printStackTrace()
            Thread.sleep(2)
        }
    }
}

private fun sendMessage(queueUrl: String, message: String) {
    while (true) {
        try {
            val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localstack:4576", "eu-west-1")
                )
                .build()
            sqs.sendMessage(SendMessageRequest(queueUrl, message))
            return
        } catch (e: SdkClientException) {
            e.printStackTrace()
            Thread.sleep(2)
        }
    }
}