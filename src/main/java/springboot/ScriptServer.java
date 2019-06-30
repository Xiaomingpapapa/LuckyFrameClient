package springboot;

import feign.Headers;
import feign.RequestLine;
import luckyclient.planapi.entity.TestTaskexcute;

/**
 * @author 33053
 * @create 2019-06-30 16:01
 * <>
 */
public interface ScriptServer {
    @RequestLine("POST /runtask")
    @Headers("Content-Type: application/json")
    void runTask(TestTaskexcute taskexcute);
}
