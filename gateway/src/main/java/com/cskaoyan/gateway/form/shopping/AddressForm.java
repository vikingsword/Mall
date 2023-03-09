package com.cskaoyan.gateway.form.shopping;

import lombok.Data;

/**
 * create by cskaoyan
 * date:2019/8/10
 */
@Data
public class AddressForm {

	private Long addressId;

	private String userName;

	private String tel;

	private String streetName;

	private boolean _Default;
}
