/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.grpc.proto;

import io.grpc.stub.ClientCalls;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.22.0)",
    comments = "Source: sample.proto")
public final class MyServiceGrpc {

  private MyServiceGrpc() {}

  public static final String SERVICE_NAME = "MyService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request,
      com.google.protobuf.Empty> getSendMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "send",
      requestType = io.siddhi.extension.io.grpc.proto.Request.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request,
      com.google.protobuf.Empty> getSendMethod() {
    io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request, com.google.protobuf.Empty> getSendMethod;
    if ((getSendMethod = MyServiceGrpc.getSendMethod) == null) {
      synchronized (MyServiceGrpc.class) {
        if ((getSendMethod = MyServiceGrpc.getSendMethod) == null) {
          MyServiceGrpc.getSendMethod = getSendMethod =
              io.grpc.MethodDescriptor.<io.siddhi.extension.io.grpc.proto.Request, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "MyService", "send"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.extension.io.grpc.proto.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new MyServiceMethodDescriptorSupplier("send"))
                  .build();
          }
        }
     }
     return getSendMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request,
      io.siddhi.extension.io.grpc.proto.Response> getProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "process",
      requestType = io.siddhi.extension.io.grpc.proto.Request.class,
      responseType = io.siddhi.extension.io.grpc.proto.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request,
      io.siddhi.extension.io.grpc.proto.Response> getProcessMethod() {
    io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.Request, io.siddhi.extension.io.grpc.proto.Response> getProcessMethod;
    if ((getProcessMethod = MyServiceGrpc.getProcessMethod) == null) {
      synchronized (MyServiceGrpc.class) {
        if ((getProcessMethod = MyServiceGrpc.getProcessMethod) == null) {
          MyServiceGrpc.getProcessMethod = getProcessMethod =
              io.grpc.MethodDescriptor.<io.siddhi.extension.io.grpc.proto.Request, io.siddhi.extension.io.grpc.proto.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "MyService", "process"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.extension.io.grpc.proto.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.extension.io.grpc.proto.Response.getDefaultInstance()))
                  .setSchemaDescriptor(new MyServiceMethodDescriptorSupplier("process"))
                  .build();
          }
        }
     }
     return getProcessMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.RequestWithMap,
      io.siddhi.extension.io.grpc.proto.ResponseWithMap> getTestMapMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "testMap",
      requestType = io.siddhi.extension.io.grpc.proto.RequestWithMap.class,
      responseType = io.siddhi.extension.io.grpc.proto.ResponseWithMap.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.RequestWithMap,
      io.siddhi.extension.io.grpc.proto.ResponseWithMap> getTestMapMethod() {
    io.grpc.MethodDescriptor<io.siddhi.extension.io.grpc.proto.RequestWithMap, io.siddhi.extension.io.grpc.proto.ResponseWithMap> getTestMapMethod;
    if ((getTestMapMethod = MyServiceGrpc.getTestMapMethod) == null) {
      synchronized (MyServiceGrpc.class) {
        if ((getTestMapMethod = MyServiceGrpc.getTestMapMethod) == null) {
          MyServiceGrpc.getTestMapMethod = getTestMapMethod =
              io.grpc.MethodDescriptor.<io.siddhi.extension.io.grpc.proto.RequestWithMap, io.siddhi.extension.io.grpc.proto.ResponseWithMap>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "MyService", "testMap"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.extension.io.grpc.proto.RequestWithMap.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.extension.io.grpc.proto.ResponseWithMap.getDefaultInstance()))
                  .setSchemaDescriptor(new MyServiceMethodDescriptorSupplier("testMap"))
                  .build();
          }
        }
     }
     return getTestMapMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MyServiceStub newStub(io.grpc.Channel channel) {
    return new MyServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new MyServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new MyServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class MyServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void send(io.siddhi.extension.io.grpc.proto.Request request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getSendMethod(), responseObserver);
    }

    /**
     */
    public void process(io.siddhi.extension.io.grpc.proto.Request request,
        io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(getProcessMethod(), responseObserver);
    }

    /**
     */
    public void testMap(io.siddhi.extension.io.grpc.proto.RequestWithMap request,
        io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.ResponseWithMap> responseObserver) {
      asyncUnimplementedUnaryCall(getTestMapMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSendMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.siddhi.extension.io.grpc.proto.Request,
                com.google.protobuf.Empty>(
                  this, METHODID_SEND)))
          .addMethod(
            getProcessMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.siddhi.extension.io.grpc.proto.Request,
                io.siddhi.extension.io.grpc.proto.Response>(
                  this, METHODID_PROCESS)))
          .addMethod(
            getTestMapMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.siddhi.extension.io.grpc.proto.RequestWithMap,
                io.siddhi.extension.io.grpc.proto.ResponseWithMap>(
                  this, METHODID_TEST_MAP)))
          .build();
    }
  }

  /**
   */
  public static final class MyServiceStub extends io.grpc.stub.AbstractStub<MyServiceStub> {
    private MyServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MyServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MyServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MyServiceStub(channel, callOptions);
    }

    /**
     */
    public void send(io.siddhi.extension.io.grpc.proto.Request request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void process(io.siddhi.extension.io.grpc.proto.Request request,
        io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.Response> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void testMap(io.siddhi.extension.io.grpc.proto.RequestWithMap request,
        io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.ResponseWithMap> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTestMapMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class MyServiceBlockingStub extends io.grpc.stub.AbstractStub<MyServiceBlockingStub> {
    private MyServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MyServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MyServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MyServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty send(io.siddhi.extension.io.grpc.proto.Request request) {
      return blockingUnaryCall(
          getChannel(), getSendMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.siddhi.extension.io.grpc.proto.Response process(io.siddhi.extension.io.grpc.proto.Request request) {
      return blockingUnaryCall(
          getChannel(), getProcessMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.siddhi.extension.io.grpc.proto.ResponseWithMap testMap(io.siddhi.extension.io.grpc.proto.RequestWithMap request) {
      return blockingUnaryCall(
          getChannel(), getTestMapMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class MyServiceFutureStub extends io.grpc.stub.AbstractStub<MyServiceFutureStub> {
    private MyServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MyServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MyServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MyServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> send(
        io.siddhi.extension.io.grpc.proto.Request request) {
      return futureUnaryCall(
          getChannel().newCall(getSendMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.siddhi.extension.io.grpc.proto.Response> process(
        io.siddhi.extension.io.grpc.proto.Request request) {
      return futureUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.siddhi.extension.io.grpc.proto.ResponseWithMap> testMap(
        io.siddhi.extension.io.grpc.proto.RequestWithMap request) {
      return futureUnaryCall(
          getChannel().newCall(getTestMapMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND = 0;
  private static final int METHODID_PROCESS = 1;
  private static final int METHODID_TEST_MAP = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MyServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MyServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND:
          serviceImpl.send((io.siddhi.extension.io.grpc.proto.Request) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_PROCESS:
          serviceImpl.process((io.siddhi.extension.io.grpc.proto.Request) request,
              (io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.Response>) responseObserver);
          break;
        case METHODID_TEST_MAP:
          serviceImpl.testMap((io.siddhi.extension.io.grpc.proto.RequestWithMap) request,
              (io.grpc.stub.StreamObserver<io.siddhi.extension.io.grpc.proto.ResponseWithMap>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class MyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MyServiceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.siddhi.extension.io.grpc.proto.Sample.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MyService");
    }
  }

  private static final class MyServiceFileDescriptorSupplier
      extends MyServiceBaseDescriptorSupplier {
    MyServiceFileDescriptorSupplier() {}
  }

  private static final class MyServiceMethodDescriptorSupplier
      extends MyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MyServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MyServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MyServiceFileDescriptorSupplier())
              .addMethod(getSendMethod())
              .addMethod(getProcessMethod())
              .addMethod(getTestMapMethod())
              .build();
        }
      }
    }
    return result;
  }
}
