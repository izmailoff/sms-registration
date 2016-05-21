package com.smsreg.user.registration

// FIXME: change phone to string here and in shell scripts

// TODO: add more props to registration request like OS version, phone type, etc
/**
 * Request that user submits in order to register a new account.
 */
case class RegistrationRequest(deviceId: String, phoneNumber: String) {
  require(deviceId.length <= 128 && deviceId.length > 5, "Device ID must be 5-128 chars long") // TODO: review this later
  // TODO: add regex for the phone num
}

case class RegistrationValidation(deviceId: String, pin: Int) {
  require(deviceId.length <= 128 && deviceId.length > 5, "Device ID must be 5-128 chars long")
}
