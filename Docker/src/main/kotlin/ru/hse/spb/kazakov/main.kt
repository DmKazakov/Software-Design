package ru.hse.spb.kazakov

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        startQueues()
    } else if (args.size == 2) {
        resendQueueMessages(args[0], args[1])
    }
}

fun resendQueueMessages(prodQueueName: String, consQueueName: String) {
    val sqs = AmazonSQSClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "eu-west-1"))
        .build()
    val prodQueueUrl = sqs.getQueueUrl(GetQueueUrlRequest(prodQueueName)).queueUrl
    val consQueueUrl = sqs.getQueueUrl(GetQueueUrlRequest(consQueueName)).queueUrl

    while (true) {
        sqs.receiveMessage(ReceiveMessageRequest(prodQueueUrl)).messages.forEach {
            val num = it.body.toInt()
            println(num)
            sqs.sendMessage(SendMessageRequest(consQueueUrl, (num + 1).toString()))
            sqs.deleteMessage(DeleteMessageRequest(consQueueUrl, it.receiptHandle))
        }
    }
}

fun startQueues() {
    val sqs = AmazonSQSClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "eu-west-1"))
        .build()
    val createQueueRequestA = CreateQueueRequest("A")
    val createQueueRequestB = CreateQueueRequest("B")
    val queueUrl = sqs.createQueue(createQueueRequestA).queueUrl
    sqs.createQueue(createQueueRequestB)
    sqs.sendMessage(SendMessageRequest(queueUrl, "1"))
}