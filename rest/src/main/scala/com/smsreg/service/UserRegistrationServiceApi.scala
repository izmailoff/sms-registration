package com.example.service

import com.github.izmailoff.logging.AkkaLoggingHelper
import com.github.izmailoff.marshalling.box.BoxMarshalling
import com.github.izmailoff.marshalling.objectid.ObjectIdSupport
import com.github.izmailoff.service.RestService
import com.smsreg.service.marshalling.CustomMarshallers

trait UserRegistrationServiceApi
  extends RestService
  with BoxMarshalling
  with ObjectIdSupport
  with AkkaLoggingHelper
  with CustomMarshallers

