org = {
  'jboss' : {
    'errai' : {
      'ioc' : {
        'tests' : {
          'wiring' : {
            'client' : {
              'res' : {
                'NativeType' : function() {
                  this.overloaded = function() {}
                }
              }
            }
          }
        }
      }
    }
  }
};

errai = {
  'get' : function() {
    return new org.jboss.errai.ioc.tests.wiring.client.res.NativeType();
  }
};
