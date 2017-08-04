/*
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.bestellung.config;

import lombok.val;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Logging von Methodenaufrufen durch AspectJ
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Aspect
@Component
@SuppressWarnings({"PMD.UnusedPrivateMethod", "squid:UnusedPrivateMethod"})
class LogAspect {
    private static final String METHOD_JOIN_POINT =
        "execution(* de.hska.*.rest.*Controller.*(..))"
        + " || execution(* de.hska.*.service.*Service*.*(..))"
        + " || execution(* de.hska.*.service.*Listener*.*(..))";

    private static final String COUNT = "Anzahl: ";
    // bei Collections wird ab 5 Elementen nur die Anzahl ausgegeben
    private static final int MAX_ELEM = 4;
    
    private static final int STRING_BUILDER_INITIAL_SIZE = 64;
    private static final String PARAM_SEPARATOR = ", ";
    private static final int PARAM_SEPARATOR_LENGTH = PARAM_SEPARATOR.length();

    private static final Map<Class<?>, Logger> LOGGER_MAP =
        new ConcurrentHashMap<>();
    
    @Around(METHOD_JOIN_POINT)
    @SuppressWarnings("checkstyle:IllegalThrows")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        final val object = joinPoint.getTarget();
        final val clazz = object.getClass();
        final val log = LOGGER_MAP.computeIfAbsent(clazz,
                                                   key -> getLogger(clazz));

        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        final val args = joinPoint.getArgs();
        final val methodName = joinPoint.getSignature().getName();
        
        // Methodenaufruf protokollieren
        logMethodBegin(log, methodName, args);

        // Eigentlicher Methodenaufruf
        final val result = joinPoint.proceed();

        // Ende der eigentlichen Methode protokollieren
        logMethodEnd(log, methodName, result);
        
        return result;

    }
    
    @SuppressWarnings("PMD.UseVarargs")
    private static void logMethodBegin(Logger log, String methodName,
                                       Object[] args) {
        String argsStr = "";
        if (args != null) {
            final val sb = new StringBuilder(STRING_BUILDER_INITIAL_SIZE);
            final val anzahlArgs = args.length;
            sb.append(": ");
            IntStream.range(0, anzahlArgs)
                     .forEach(i -> {
                final val argStr = args[i] == null ? "null" : toString(args[i]);
                sb.append(argStr);
                sb.append(PARAM_SEPARATOR);
            });
            final val laenge = sb.length();
            sb.delete(laenge - PARAM_SEPARATOR_LENGTH, laenge - 1);
            argsStr = sb.toString();
        }
        log.debug(methodName + " BEGINN" + argsStr);
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private static void logMethodEnd(Logger log, String methodName,
                                     Object result) {
        final val endStr = result == null
                           ? methodName + " ENDE"
                           : methodName + " ENDE: " + toString(result);
        log.debug(endStr);
    }

    /**
     * Collection oder Array oder Objekt in einen String konvertieren
     * @param obj Eine Collection, ein Array oder ein sonstiges Objekt
     * @return obj als String
     */
    private static String toString(Object obj) {
        if (obj instanceof Collection<?>) {
            // die Elemente nur bei kleiner Anzahl ausgeben
            // sonst nur die Anzahl der Elemente
            final Collection<?> coll = (Collection<?>) obj;
            final val anzahl = coll.size();
            if (anzahl > MAX_ELEM) {
                return COUNT + coll.size();
            }

            return coll.toString();
        }
        
        if (obj.getClass().isArray()) {
            // Array in String konvertieren: Element fuer Element
            return arrayToString(obj);
        }

        // Objekt, aber keine Collection und kein Array
        return obj.toString();
    }
    
    /**
     * Array in einen String konvertieren
     * @param obj ein Array
     * @return das Array als String
     */
    private static String arrayToString(Object obj) {
        final val componentClass = obj.getClass().getComponentType();

        if (!componentClass.isPrimitive()) {
            return arrayOfObject(obj);
        }
        
        // Array von primitiven Werten: byte, short, int, long, float, double,
        // boolean, char
        final val className = componentClass.getName();
        return arrayOfPrimitive(obj, className);
    }
    
    private static String arrayOfObject(Object obj) {
        final Object[] arr = (Object[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);        
    }
    
    private static String arrayOfPrimitive(Object obj, String className) {
        switch (className) {
            case "byte":
                return arrayOfByte(obj);
            case "short":
                return arrayOfShort(obj);
            case "int":
                return arrayOfInt(obj);
            case "long":
                return arrayOfLong(obj);
            case "float":
                return arrayOfFloat(obj);
            case "double":
                return arrayOfDouble(obj);
            case "boolean":
                return arrayOfBoolean(obj);
            case "char":
                return arrayOfChar(obj);
            default:
                return "<<UNKNOWN PRIMITIVE ARRAY>>";
        }
    }
    
    private static String arrayOfByte(Object obj) {
        final byte[] arr = (byte[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfShort(Object obj) {
        final short[] arr = (short[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfInt(Object obj) {
        final int[] arr = (int[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfLong(Object obj) {
        final long[] arr = (long[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfFloat(Object obj) {
        final float[] arr = (float[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfDouble(Object obj) {
        final double[] arr = (double[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfBoolean(Object obj) {
        final boolean[] arr = (boolean[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
    
    private static String arrayOfChar(Object obj) {
        final char[] arr = (char[]) obj;
        if (arr.length > MAX_ELEM) {
            return COUNT + arr.length;
        }
        return Arrays.toString(arr);
    }
}
