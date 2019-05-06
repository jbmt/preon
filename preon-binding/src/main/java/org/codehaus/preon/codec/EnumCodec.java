/**
 * Copyright (c) 2009-2016 Wilfred Springer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.codehaus.preon.codec;

import org.codehaus.preon.*;
import org.codehaus.preon.annotation.BoundNumber;
import org.codehaus.preon.buffer.BitBuffer;
import org.codehaus.preon.buffer.ByteOrder;
import org.codehaus.preon.channel.BitChannel;
import org.codehaus.preon.el.Expression;
import org.codehaus.preon.el.Expressions;
import org.codehaus.preon.util.EnumUtils;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: wilfred Date: Oct 24, 2009 Time: 5:03:58 PM To change this template use File |
 * Settings | File Templates.
 */
public class EnumCodec<T> implements Codec<T> {

    private final Class<T> type;

    private final Map<Long, T> mapping;

    private final Map<T, Long> inverseMapping;

    private final Expression<Integer, Resolver> size;

    private final ByteOrder byteOrder;

    public EnumCodec(Class<T> type, Map<Long, T> mapping,
                     Expression<Integer, Resolver> sizeExpr, ByteOrder endian) {
        assert type != null;
        assert mapping != null;
        assert sizeExpr != null;
        assert endian != null;
        this.type = type;
        this.mapping = mapping;
        this.size = sizeExpr;
        this.byteOrder = endian;
        inverseMapping = new HashMap<T, Long>();
        for (Map.Entry<Long, T> entry : mapping.entrySet()) {
            inverseMapping.put(entry.getValue(), entry.getKey());
        }
    }

    public T decode(BitBuffer buffer, Resolver resolver, Builder builder)
            throws DecodingException {
        long value = buffer.readAsLong(size.eval(resolver), byteOrder);
        T result = mapping.get(value);
        if (result == null) {
            result = mapping.get(null);
        }
        return result;
    }

    public void encode(T object, BitChannel channel, Resolver resolver) throws IOException {
        channel.write(size.eval(resolver), inverseMapping.get(object), byteOrder);
    }

    public Class<?>[] getTypes() {
        return new Class[]{type};
    }

    public Expression<Integer, Resolver> getSize() {
        return size;
    }

    public Class<?> getType() {
        return type;
    }

    /**
     * A {@link org.codehaus.preon.CodecFactory} creating {@link org.codehaus.preon.Codec Codecs} capable of decoding enum
     * values. At this state, it will be triggered by enum type of fields with a {@link
     * org.codehaus.preon.annotation.BoundNumber} annotation, to pass metadata on the number of bits (and endianness) from
     * which the enum's value needs to get constructed.
     *
     * @author Wilfred Springer
     */
    public static class Factory implements CodecFactory {

        public <T> Codec<T> create(AnnotatedElement metadata, Class<T> type,
                                   ResolverContext context) {
            if (type.isEnum() && metadata.isAnnotationPresent(BoundNumber.class)) {
                Map<Long, T> mapping = EnumUtils.getBoundEnumOptionIndex(type);
                BoundNumber settings = metadata.getAnnotation(BoundNumber.class);
                Expression<Integer, Resolver> sizeExpr = Expressions.createInteger(
                        context, settings.size());
                return new EnumCodec<T>(type, mapping, sizeExpr, settings
                        .byteOrder());

            } else {
                return null;
            }
        }

    }
}
