/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.server

import kafka.testkit.{KafkaClusterTestKit, TestKitNodes}
import kafka.utils.TestUtils
import org.apache.kafka.clients.admin.{Admin, NewTopic}
import org.apache.kafka.metadata.BrokerState
import org.junit.rules.Timeout
import org.junit.{Assert, Rule, Test}

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.compat.java8.OptionConverters

class Kip500ClusterTest {
  @Rule
  def globalTimeout = Timeout.millis(120000)

  @Test
  def testCreateClusterAndClose(): Unit = {
    val cluster = new KafkaClusterTestKit.Builder(
      new TestKitNodes.Builder().
        setNumKip500BrokerNodes(1).
        setNumControllerNodes(1).build()).build()
    try {
      cluster.format()
      cluster.startup()
    } finally {
      cluster.close()
    }
  }

  @Test
  def testCreateClusterAndWaitForBrokerInRunningState(): Unit = {
    val cluster = new KafkaClusterTestKit.Builder(
      new TestKitNodes.Builder().
        setNumKip500BrokerNodes(3).
        setNumControllerNodes(3).build()).build()
    try {
      cluster.format()
      cluster.startup()
      TestUtils.waitUntilTrue(() => cluster.kip500Brokers().get(0).currentState() == BrokerState.RUNNING,
        "Broker never made it to RUNNING state.")
      TestUtils.waitUntilTrue(() => OptionConverters.toJava(cluster.raftManagers().get(0).currentLeader).isPresent,
      "RaftManager was not initialized.")
      val admin = Admin.create(cluster.clientProperties())
      try {
        Assert.assertEquals(cluster.nodes().clusterId().toString,
          admin.describeCluster().clusterId().get())
      } finally {
        admin.close()
      }
    } finally {
      cluster.close()
    }
  }

  @Test
  def testCreateClusterAndCreateAndListTopic(): Unit = {
    val cluster = new KafkaClusterTestKit.Builder(
      new TestKitNodes.Builder().
        setNumKip500BrokerNodes(3).
        setNumControllerNodes(3).build()).build()
    try {
      cluster.format()
      cluster.startup()
      cluster.waitForReadyBrokers()
      TestUtils.waitUntilTrue(() => cluster.kip500Brokers().get(0).currentState() == BrokerState.RUNNING,
        "Broker never made it to RUNNING state.")
      TestUtils.waitUntilTrue(() => OptionConverters.toJava(cluster.raftManagers().get(0).currentLeader).isPresent,
        "RaftManager was not initialized.")
      val admin = Admin.create(cluster.clientProperties())
      try {
        // Create a test topic
        val newTopic = Collections.singletonList(new NewTopic("test-topic", 1, 3.toShort))
        val createTopicResult = admin.createTopics(newTopic)
        createTopicResult.all().get(60, TimeUnit.SECONDS)

        // List created topic
        TestUtils.waitUntilTrue(() => {
          val listTopicsResult = admin.listTopics()
          val result = listTopicsResult.names().get(5, TimeUnit.SECONDS).size() == newTopic.size()
          if (result) {
            newTopic forEach(topic => {
              Assert.assertTrue(listTopicsResult.names().get().contains(topic.name()))
            })
          }
          result
        }, "Topics created were not listed.")
      } finally {
        admin.close()
      }
    } finally {
      cluster.close()
    }
  }

  @Test
  def testCreateClusterAndCreateAndManyTopics(): Unit = {
    val cluster = new KafkaClusterTestKit.Builder(
      new TestKitNodes.Builder().
        setNumKip500BrokerNodes(3).
        setNumControllerNodes(3).build()).build()
    try {
      cluster.format()
      cluster.startup()
      cluster.waitForReadyBrokers()
      TestUtils.waitUntilTrue(() => cluster.kip500Brokers().get(0).currentState() == BrokerState.RUNNING,
        "Broker never made it to RUNNING state.")
      TestUtils.waitUntilTrue(() => OptionConverters.toJava(cluster.raftManagers().get(0).currentLeader).isPresent,
        "RaftManager was not initialized.")
      val admin = Admin.create(cluster.clientProperties())
      try {
        // Create many topics
        val newTopic = new util.ArrayList[NewTopic]()
        newTopic.add(new NewTopic("test-topic-1", 1, 3.toShort))
        newTopic.add(new NewTopic("test-topic-2", 1, 3.toShort))
        newTopic.add(new NewTopic("test-topic-3", 1, 3.toShort))
        val createTopicResult = admin.createTopics(newTopic)
        createTopicResult.all().get(60, TimeUnit.SECONDS)

        // List created topic
        TestUtils.waitUntilTrue(() => {
          val listTopicsResult = admin.listTopics()
          val result = listTopicsResult.names().get(5, TimeUnit.SECONDS).size() == newTopic.size()
          if (result) {
            newTopic forEach(topic => {
              Assert.assertTrue(listTopicsResult.names().get().contains(topic.name()))
            })
          }
          result
        }, "Topics created were not listed.")
      } finally {
        admin.close()
      }
    } finally {
      cluster.close()
    }
  }

  @Test
  def testCreateClusterAndCreateAndManyTopicsWithManyPartitions(): Unit = {
    val cluster = new KafkaClusterTestKit.Builder(
      new TestKitNodes.Builder().
        setNumKip500BrokerNodes(3).
        setNumControllerNodes(3).build()).build()
    try {
      cluster.format()
      cluster.startup()
      cluster.waitForReadyBrokers()
      TestUtils.waitUntilTrue(() => cluster.kip500Brokers().get(0).currentState() == BrokerState.RUNNING,
        "Broker never made it to RUNNING state.")
      TestUtils.waitUntilTrue(() => OptionConverters.toJava(cluster.raftManagers().get(0).currentLeader).isPresent,
        "RaftManager was not initialized.")
      val admin = Admin.create(cluster.clientProperties())
      try {
        // Create many topics
        val newTopic = new util.ArrayList[NewTopic]()
        newTopic.add(new NewTopic("test-topic-1", 3, 3.toShort))
        newTopic.add(new NewTopic("test-topic-2", 3, 3.toShort))
        newTopic.add(new NewTopic("test-topic-3", 3, 3.toShort))
        val createTopicResult = admin.createTopics(newTopic)
        createTopicResult.all().get(60, TimeUnit.SECONDS)

        // List created topic
        TestUtils.waitUntilTrue(() => {
          val listTopicsResult = admin.listTopics()
          val result = listTopicsResult.names().get(5, TimeUnit.SECONDS).size() == newTopic.size()
          if (result) {
            newTopic forEach(topic => {
              Assert.assertTrue(listTopicsResult.names().get().contains(topic.name()))
            })
          }
          result
        }, "Topics created were not listed.")
      } finally {
        admin.close()
      }
    } finally {
      cluster.close()
    }
  }
}