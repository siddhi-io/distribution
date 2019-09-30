package org.wso2.grpc;

import io.grpc.stub.ClientCalls;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.21.0)",
    comments = "Source: EventService.proto")
public final class EventServiceGrpc {

  private EventServiceGrpc() {}

  public static final String SERVICE_NAME = "eventservice.EventService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.wso2.grpc.Event,
      org.wso2.grpc.Event> getProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "process",
      requestType = org.wso2.grpc.Event.class,
      responseType = org.wso2.grpc.Event.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.wso2.grpc.Event,
      org.wso2.grpc.Event> getProcessMethod() {
    io.grpc.MethodDescriptor<org.wso2.grpc.Event, org.wso2.grpc.Event> getProcessMethod;
    if ((getProcessMethod = EventServiceGrpc.getProcessMethod) == null) {
      synchronized (EventServiceGrpc.class) {
        if ((getProcessMethod = EventServiceGrpc.getProcessMethod) == null) {
          EventServiceGrpc.getProcessMethod = getProcessMethod = 
              io.grpc.MethodDescriptor.<org.wso2.grpc.Event, org.wso2.grpc.Event>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "eventservice.EventService", "process"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.grpc.Event.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.grpc.Event.getDefaultInstance()))
                  .setSchemaDescriptor(new EventServiceMethodDescriptorSupplier("process"))
                  .build();
          }
        }
     }
     return getProcessMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.wso2.grpc.Event,
      com.google.protobuf.Empty> getConsumeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "consume",
      requestType = org.wso2.grpc.Event.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<org.wso2.grpc.Event,
      com.google.protobuf.Empty> getConsumeMethod() {
    io.grpc.MethodDescriptor<org.wso2.grpc.Event, com.google.protobuf.Empty> getConsumeMethod;
    if ((getConsumeMethod = EventServiceGrpc.getConsumeMethod) == null) {
      synchronized (EventServiceGrpc.class) {
        if ((getConsumeMethod = EventServiceGrpc.getConsumeMethod) == null) {
          EventServiceGrpc.getConsumeMethod = getConsumeMethod = 
              io.grpc.MethodDescriptor.<org.wso2.grpc.Event, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "eventservice.EventService", "consume"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.grpc.Event.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new EventServiceMethodDescriptorSupplier("consume"))
                  .build();
          }
        }
     }
     return getConsumeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EventServiceStub newStub(io.grpc.Channel channel) {
    return new EventServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EventServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new EventServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EventServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new EventServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class EventServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void process(org.wso2.grpc.Event request,
        io.grpc.stub.StreamObserver<org.wso2.grpc.Event> responseObserver) {
      asyncUnimplementedUnaryCall(getProcessMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.wso2.grpc.Event> consume(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return asyncUnimplementedStreamingCall(getConsumeMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getProcessMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.wso2.grpc.Event,
                org.wso2.grpc.Event>(
                  this, METHODID_PROCESS)))
          .addMethod(
            getConsumeMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                org.wso2.grpc.Event,
                com.google.protobuf.Empty>(
                  this, METHODID_CONSUME)))
          .build();
    }
  }

  /**
   */
  public static final class EventServiceStub extends io.grpc.stub.AbstractStub<EventServiceStub> {
    private EventServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected EventServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceStub(channel, callOptions);
    }

    /**
     */
    public void process(org.wso2.grpc.Event request,
        io.grpc.stub.StreamObserver<org.wso2.grpc.Event> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.wso2.grpc.Event> consume(
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      return ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getConsumeMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class EventServiceBlockingStub extends io.grpc.stub.AbstractStub<EventServiceBlockingStub> {
    private EventServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected EventServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.wso2.grpc.Event process(org.wso2.grpc.Event request) {
      return blockingUnaryCall(
          getChannel(), getProcessMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EventServiceFutureStub extends io.grpc.stub.AbstractStub<EventServiceFutureStub> {
    private EventServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected EventServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.wso2.grpc.Event> process(
        org.wso2.grpc.Event request) {
      return futureUnaryCall(
          getChannel().newCall(getProcessMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROCESS = 0;
  private static final int METHODID_CONSUME = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EventServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EventServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PROCESS:
          serviceImpl.process((org.wso2.grpc.Event) request,
              (io.grpc.stub.StreamObserver<org.wso2.grpc.Event>) responseObserver);
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
        case METHODID_CONSUME:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.consume(
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class EventServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EventServiceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.wso2.grpc.EventServiceOuterClass.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EventService");
    }
  }

  private static final class EventServiceFileDescriptorSupplier
      extends EventServiceBaseDescriptorSupplier {
    EventServiceFileDescriptorSupplier() {}
  }

  private static final class EventServiceMethodDescriptorSupplier
      extends EventServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EventServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (EventServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EventServiceFileDescriptorSupplier())
              .addMethod(getProcessMethod())
              .addMethod(getConsumeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
