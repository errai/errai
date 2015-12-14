NativeConcreteJsType = function() {
  this.message = function() {
    return "I am a native type!";
  };
};

ProducedNativeIface = function() {
  this.getMagicWord = function() {
    return "please";
  }
};
  
NativeFactory = function() {
};

NativeFactory.create = function() {
  return new ProducedNativeIface();
};
