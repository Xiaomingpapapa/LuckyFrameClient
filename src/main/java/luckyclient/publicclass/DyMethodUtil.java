package luckyclient.publicclass;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.Map;

/**
 * @author 33053
 * @create 2019/3/10 17:01
 * <>
 */
public class DyMethodUtil {
    public static Object invokeMethod(String jexlExp, Map<String,Object> map){
        JexlEngine jexl=new JexlEngine();
        Expression e = jexl.createExpression(jexlExp);
        JexlContext jc = new MapContext();
        for(String key:map.keySet()){
            jc.set(key, map.get(key));
        }
        if(null==e.evaluate(jc)){
            return "";
        }
        return e.evaluate(jc);
    }
}
