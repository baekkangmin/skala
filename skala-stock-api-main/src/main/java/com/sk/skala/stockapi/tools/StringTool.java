package com.sk.skala.stockapi.tools;

import java.util.regex.Pattern;

public class StringTool {

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isAnyEmpty(String... strings) {
		for (String str : strings) {
			if (str == null || str.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public static String join(String... strings) {
		StringBuffer buff = new StringBuffer();
		int index = 0;
		for (String str : strings) {
			if (index > 0) {
				buff.append(",");
				buff.append(str);
			} else {
				buff.append(str);
			}
			index++;
		}
		return buff.toString();
	}

	public static String removeInjection(String source) {
		Pattern specials = Pattern.compile("['\"\\-#()@;=*/+]");
		String dest = specials.matcher(source).replaceAll("");

		Pattern sql = Pattern.compile("(union|select|from|where)", Pattern.CASE_INSENSITIVE);
		dest = sql.matcher(dest).replaceAll("");

		return dest;
	}

	public static String like(String string) {
		return "%" + string + "%";
	}

	public static String toCamel(String source) {
		if (isEmpty(source)) {
			return "";
		}

		StringBuilder builder = new StringBuilder(source);
		for (int i = 0; i < builder.length(); i++) {
			if (builder.charAt(i) == '_') {
				builder.deleteCharAt(i);
				if (i < builder.length()) {
					builder.setCharAt(i, Character.toUpperCase(builder.charAt(i)));
				}
			}
		}
		return builder.toString();
	}

	public static String toSnake(String source) {
		if (isEmpty(source)) {
			return "";
		}

		StringBuffer buff = new StringBuffer();
		buff.append(Character.toLowerCase(source.charAt(0)));
		for (int i = 1; i < source.length(); i++) {
			if (Character.isUpperCase(source.charAt(i))) {
				buff.append('_');
				buff.append(Character.toLowerCase(source.charAt(i)));
			} else {
				buff.append(Character.toLowerCase(source.charAt(i)));
			}
		}
		return buff.toString().toLowerCase();
	}
}
