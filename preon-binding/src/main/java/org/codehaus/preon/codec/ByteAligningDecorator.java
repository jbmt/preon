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
import org.codehaus.preon.annotation.ByteAlign;
import org.codehaus.preon.buffer.BitBuffer;
import org.codehaus.preon.channel.BitChannel;
import org.codehaus.preon.el.Expression;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;

/**
 * A {@link CodecDecorator} that will make sure that reading stops at a byte-aligned position.
 *
 * @author Wilfred Springer
 */
public class ByteAligningDecorator implements CodecDecorator {

    public <T> Codec<T> decorate(Codec<T> decorated, AnnotatedElement metadata,
                                 Class<T> type, ResolverContext context) {
        if (type.isAnnotationPresent(ByteAlign.class)
                || (metadata != null && metadata
                .isAnnotationPresent(ByteAlign.class))) {
            return new ByteAligningCodec<T>(decorated);
        } else {
            return decorated;
        }
    }

    private static class ByteAligningCodec<T> implements Codec<T> {

        private Codec<T> decorated;

        public ByteAligningCodec(Codec<T> decorated) {
            this.decorated = decorated;
        }

        public T decode(BitBuffer buffer, Resolver resolver, Builder builder)
                throws DecodingException {
            T result = decorated.decode(buffer, resolver, builder);
            long pos = buffer.getBitPos() % 8;
            if (pos > 0) {
                buffer.setBitPos(buffer.getBitPos() + 8 - pos);
            }
            return result;
        }

        public void encode(T object, BitChannel channel, Resolver resolver) throws IOException {
            int bits = 8 - channel.getRelativeBitPos();
            if (bits != 8) {
                channel.write(bits, (byte) 0);
            }
            decorated.encode(object, channel, resolver);
        }

        public Class<?>[] getTypes() {
            return decorated.getTypes();
        }

        public Expression<Integer, Resolver> getSize() {
            return null;
        }

        public Class<?> getType() {
            return decorated.getType();
        }

    }

}
