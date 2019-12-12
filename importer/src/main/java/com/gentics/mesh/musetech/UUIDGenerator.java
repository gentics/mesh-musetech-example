package com.gentics.mesh.musetech;

import com.gentics.mesh.util.UUIDUtil;

public class UUIDGenerator {

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(UUIDUtil.randomUUID());
		}
	}

}
