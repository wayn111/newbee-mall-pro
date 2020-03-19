package ltd.newbee.mall.config;

import com.google.code.kaptcha.text.TextProducer;
import com.google.code.kaptcha.util.Configurable;
import lombok.SneakyThrows;

import java.security.SecureRandom;

public class TextCreator extends Configurable implements TextProducer {
    /**
     * @return the random text
     */
    @SneakyThrows
    public String getText() {
        int length = getConfig().getTextProducerCharLength();
        char[] chars = getConfig().getTextProducerCharString();
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
//        Random rand = new Random();
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < length; i++) {
            text.append(chars[rand.nextInt(chars.length)]);
        }

        return text.toString();
    }
}