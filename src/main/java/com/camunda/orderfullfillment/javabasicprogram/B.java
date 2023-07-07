package com.camunda.orderfullfillment.javabasicprogram;

public class B {

	private B() {

		super();

	}

	static B simpleVariable = null;

	static B getShow() {

		if (simpleVariable == null) {

			simpleVariable = new B();

		}

		return simpleVariable;

	}

	public static void main(String[] args) {

		B sc = new B();

		B sc1 = new B();

		System.out.println("Non-singleton  " + sc.hashCode());

		System.out.println("Non-singleton  " + sc1.hashCode());

		B obj = B.getShow();

		B obj1 = B.getShow();

		System.out.println(obj.hashCode());

		System.out.println(obj1.hashCode());

	}

}
