package rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.common.ReflectionUtils;
import org.axonframework.messaging.responsetypes.AbstractResponseType;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryResponseMessage;

import java.beans.ConstructorProperties;
import java.lang.reflect.Type;

// FIXME: Attempt to solve on framework level -> Axon Server Fails to de-serialize, currently unused.
public class QueryResponseMessageResponseType<R> extends AbstractResponseType<R> {

  @JsonCreator
  @ConstructorProperties({ "expectedResponseType" })
  public QueryResponseMessageResponseType(@JsonProperty("expectedResponseType") Class<R> expectedResponseType) {
    super(expectedResponseType);
  }

  @Override
  public boolean matches(Type responseType) {
    Type unwrapped = ReflectionUtils.unwrapIfType(responseType, QueryResponseMessage.class);
    return isGenericAssignableFrom(unwrapped) || isAssignableFrom(unwrapped);
  }

  @Override
  public R convert(Object response) {
    if (response instanceof QueryResponseMessage) {
      return ((QueryResponseMessage<R>) response).getPayload();
    }
    return (R) response;
  }

  @Override
  public Class<R> responseMessagePayloadType() {
    return (Class<R>) expectedResponseType;
  }

  /**
   * Responsible for finding the query handler based on the return type of the method.
   *
   * @return class to match the handler method return class.
   */
  @Override
  public Class<?> getExpectedResponseType() {
    return QueryResponseMessage.class;
  }

//  @Override
//  public ResponseType<?> forSerialization() {
//    return ResponseTypes.instanceOf(expectedResponseType);
//  }

  @Override
  public String toString() {
    return "QueryResponseMessageResponseType{" + expectedResponseType + "}";
  }
}
