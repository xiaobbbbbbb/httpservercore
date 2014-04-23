package com.ecarinfo.frame.httpserver.core.http.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.ClassWriter;
import aj.org.objectweb.asm.Label;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import aj.org.objectweb.asm.Type;

/**
 * <p>
 * 基于asm的工具类
 * </p>
 */
public final class AsmUtils {

	/**
	 * 
	 * <p>比较参数类型是否一致</p>
	 *
	 * @param types asm的类型({@link Type})
	 * @param clazzes java 类型({@link Class})
	 * @return
	 */
	private static boolean sameType(Type[] types, Class<?>[] clazzes) {
		// 个数不同
		if (types.length != clazzes.length) {
			return false;
		}

		for (int i = 0; i < types.length; i++) {
			if(!Type.getType(clazzes[i]).equals(types[i])) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 
	 * <p>获取方法的参数名</p>
	 *
	 * @param m
	 * @return
	 */
	public static String[] getMethodParamNames(final Method m) {
		final String[] paramNames = new String[m.getParameterTypes().length];
		final String n = m.getDeclaringClass().getName();
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassReader cr = null;
		try {
			cr = new ClassReader(n);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cr.accept(new ClassVisitor(Opcodes.ASM4, cw) {
			@Override
			public MethodVisitor visitMethod(final int access,
					final String name, final String desc,
					final String signature, final String[] exceptions) {
				final Type[] args = Type.getArgumentTypes(desc);
				// 方法名相同并且参数个数相同
				if (!name.equals(m.getName())
						|| !sameType(args, m.getParameterTypes())) {
					return super.visitMethod(access, name, desc, signature,
							exceptions);
				}
				MethodVisitor v = cv.visitMethod(access, name, desc, signature,
						exceptions);
				return new MethodVisitor(Opcodes.ASM4, v) {
					@Override
					public void visitLocalVariable(String name, String desc,
							String signature, Label start, Label end, int index) {
						int i = index - 1;
						// 如果是静态方法，则第一就是参数
						// 如果不是静态方法，则第一个是"this"，然后才是方法的参数
						if(Modifier.isStatic(m.getModifiers())) {
							i = index;
						}
						if (i >= 0 && i < paramNames.length) {
							paramNames[i] = name;
						}
						super.visitLocalVariable(name, desc, signature, start,
								end, index);
					}

				};
			}
		}, 0);
		return paramNames;
	}

	public static void main(String[] args) throws SecurityException,
			NoSuchMethodException {
//		String[] s = getMethodParamNames(ApiModule.class.getMethod("getUser",
//				String.class,String.class, int.class, boolean.class));
//		s = getMethodParamNames(ApiModule.class.getMethod("getUser",
//				String.class,String.class, int.class, boolean.class));
	}

}
