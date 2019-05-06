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
import org.codehaus.preon.annotation.BoundBuffer;
import org.codehaus.preon.buffer.BitBuffer;
import org.codehaus.preon.channel.BitChannel;
import org.codehaus.preon.el.Expression;
import org.codehaus.preon.el.Expressions;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;

public class BoundBufferCodecFactory implements CodecFactory {

    private Class<?> BYTE_CLASS = (new byte[0]).getClass();

    public <T> Codec<T> create(AnnotatedElement metadata, Class<T> type,
                               ResolverContext context) {
        if (type.isArray() && BYTE_CLASS.equals(type)
                && metadata.isAnnotationPresent(BoundBuffer.class)) {
            return (Codec<T>) new BoundBufferCodec(metadata.getAnnotation(
                    BoundBuffer.class).match());
        } else {
            return null;
        }
    }

    private static class BoundBufferCodec implements Codec<Object> {

        private byte[] criterion;

        public BoundBufferCodec(byte[] matches) {
            this.criterion = matches;
        }

        public Object decode(BitBuffer buffer, Resolver resolver,
                             Builder builder) throws DecodingException {
            for (int i = 0; i < criterion.length; i++) {
                if (criterion[i] != buffer.readAsByte(8)) {
                    throw new DecodingException("First " + criterion.length
                            + " bytes do not match expected value.");
                }
            }
            return criterion;
        }

        public void encode(Object object, BitChannel channel, Resolver resolver) throws IOException {
            channel.write(criterion, 0, criterion.length);
        }

        public Expression<Integer, Resolver> getSize() {
            return Expressions.createInteger(criterion.length * 8, Resolver.class);
        }

        public Class<?> getType() {
            return byte.class;
        }

        public Class<?>[] getTypes() {
            return new Class<?>[]{byte.class};
        }
    }

}
