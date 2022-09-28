package top.chukongxiang.spring.cache.tool;

import java.io.*;

/**
 * @author 楚孔响
 * @date 2022-09-28 10:37
 */
public class ByteUtil {

    public static byte[] parseToByte(Object o) throws IOException {
        ByteArrayOutputStream byam = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byam);
        oos.writeObject(o);
        byte[] bytes = byam.toByteArray();
        oos.close();
        byam.close();
        return bytes;
    }

    public static <T> T parseToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        T t = (T) ois.readObject();
        ois.close();
        bais.close();
        return t;
    }

}
