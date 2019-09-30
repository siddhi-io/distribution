package io.siddhi.distribution.sample.grpc;

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
    comments = "Source: SweetProduction.proto")
public final class SweetServiceGrpc {

  private SweetServiceGrpc() {}

  public static final String SERVICE_NAME = "SweetService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.siddhi.distribution.sample.grpc.Sweet,
      io.siddhi.distribution.sample.grpc.Sweet> getGetDiscountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getDiscount",
      requestType = io.siddhi.distribution.sample.grpc.Sweet.class,
      responseType = io.siddhi.distribution.sample.grpc.Sweet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.siddhi.distribution.sample.grpc.Sweet,
      io.siddhi.distribution.sample.grpc.Sweet> getGetDiscountMethod() {
    io.grpc.MethodDescriptor<io.siddhi.distribution.sample.grpc.Sweet, io.siddhi.distribution.sample.grpc.Sweet> getGetDiscountMethod;
    if ((getGetDiscountMethod = SweetServiceGrpc.getGetDiscountMethod) == null) {
      synchronized (SweetServiceGrpc.class) {
        if ((getGetDiscountMethod = SweetServiceGrpc.getGetDiscountMethod) == null) {
          SweetServiceGrpc.getGetDiscountMethod = getGetDiscountMethod =
              io.grpc.MethodDescriptor.<io.siddhi.distribution.sample.grpc.Sweet, io.siddhi.distribution.sample.grpc.Sweet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "SweetService", "getDiscount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.distribution.sample.grpc.Sweet.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.siddhi.distribution.sample.grpc.Sweet.getDefaultInstance()))
                  .setSchemaDescriptor(new SweetServiceMethodDescriptorSupplier("getDiscount"))
                  .build();
          }
        }
     }
     return getGetDiscountMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SweetServiceStub newStub(io.grpc.Channel channel) {
    return new SweetServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SweetServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SweetServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SweetServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SweetServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class SweetServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void getDiscount(io.siddhi.distribution.sample.grpc.Sweet request,
        io.grpc.stub.StreamObserver<io.siddhi.distribution.sample.grpc.Sweet> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDiscountMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDiscountMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.siddhi.distribution.sample.grpc.Sweet,
                io.siddhi.distribution.sample.grpc.Sweet>(
                  this, METHODID_GET_DISCOUNT)))
          .build();
    }
  }

  /**
   */
  public static final class SweetServiceStub extends io.grpc.stub.AbstractStub<SweetServiceStub> {
    private SweetServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SweetServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SweetServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SweetServiceStub(channel, callOptions);
    }

    /**
     */
    public void getDiscount(io.siddhi.distribution.sample.grpc.Sweet request,
        io.grpc.stub.StreamObserver<io.siddhi.distribution.sample.grpc.Sweet> responseObserver) {
      ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetDiscountMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class SweetServiceBlockingStub extends io.grpc.stub.AbstractStub<SweetServiceBlockingStub> {
    private SweetServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SweetServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SweetServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SweetServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.siddhi.distribution.sample.grpc.Sweet getDiscount(io.siddhi.distribution.sample.grpc.Sweet request) {
      return blockingUnaryCall(
          getChannel(), getGetDiscountMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class SweetServiceFutureStub extends io.grpc.stub.AbstractStub<SweetServiceFutureStub> {
    private SweetServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SweetServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SweetServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SweetServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.siddhi.distribution.sample.grpc.Sweet> getDiscount(
        io.siddhi.distribution.sample.grpc.Sweet request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDiscountMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_DISCOUNT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SweetServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SweetServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DISCOUNT:
          serviceImpl.getDiscount((io.siddhi.distribution.sample.grpc.Sweet) request,
              (io.grpc.stub.StreamObserver<io.siddhi.distribution.sample.grpc.Sweet>) responseObserver);
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

  private static abstract class SweetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SweetServiceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.siddhi.distribution.sample.grpc.SweetProduction.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SweetService");
    }
  }

  private static final class SweetServiceFileDescriptorSupplier
      extends SweetServiceBaseDescriptorSupplier {
    SweetServiceFileDescriptorSupplier() {}
  }

  private static final class SweetServiceMethodDescriptorSupplier
      extends SweetServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SweetServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (SweetServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SweetServiceFileDescriptorSupplier())
              .addMethod(getGetDiscountMethod())
              .build();
        }
      }
    }
    return result;
  }
}
