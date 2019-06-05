package ru.hse.spb.kazakov

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.*

fun main(args: Array<String>) {
    System.setProperty("aws.accessKeyId", "accessKey")
    System.setProperty("aws.secretKey", "secretKey")

    Thread.sleep(50)
    if (args.isEmpty()) {
        startQueues()
    } else if (args.size == 2) {
        resendQueueMessages(args[0], args[1])
    }
}

fun resendQueueMessages(prodQueueName: String, consQueueName: String) {
    while (true) {
        try {
            val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "eu-west-1")
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
            println("waiting for localstack to start")
            Thread.sleep(1)
        }
    }
}

fun startQueues() {
    while (true) {
        try {
            val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "eu-west-1")
                )
                .build()
            val createQueueRequestA = CreateQueueRequest("A")
            val createQueueRequestB = CreateQueueRequest("B")
            val queueUrl = sqs.createQueue(createQueueRequestA).queueUrl
            sqs.createQueue(createQueueRequestB)
            sqs.sendMessage(SendMessageRequest(queueUrl, "1"))
        } catch (e: Exception) {
            println("Waiting for Localstack services to start.")
            Thread.sleep(1)
        }
    }
}