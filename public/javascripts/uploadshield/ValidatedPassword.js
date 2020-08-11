define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin",
        "dijit/form/ValidationTextBox", "dojo/on", "dojo/_base/lang",  "dojo/dom-class"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, validatedTextbox, on, lang, domClass){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

            /**
             * The password field
             */
            password: null,
            /**
             * The confirmation field where we chack to make sure the values are the same
             */
            passwordConfirmation: null,


            promptMessage: null,

            verifyPassword: function(value, constraints) {


                if ( value.length < 5)
                {
                    this.promptMessage = "Password  must be at least 5 characters";
                    return false;
                }

                if ( value.length > 32 )
                {
                    this.promptMessage = "Password cannot be longer than 32 characters";
                    return false;
                }

                if (!value.match(/(?=.*[A-Z])/))
                {
                    this.promptMessage = "Password must contain at least one capital letter";
                    return false;
                }

                if (!value.match(/(?=.*\d)/))
                {
                    this.promptMessage =  "Password must contain at least one number";
                    return false;
                }

                //check to make sure there is a special character
                if (!value.match(/(?=.*[\][_!@#$%^&*()<>?/|{}\\+=-])/))
                {
                    this.promptMessage = "Password must contain a special character [!@#$%^&*()<>?/|{}\\+=-_]";
                    return false;
                }

                this.promptMessage = null;
                return true;
            },

            get: function(name) {
              return this.password.get(name);
            },

            setDisabled: function(disabled) {
              this.password.setDisabled(disabled);
              this.passwordConfirmation.setDisabled(disabled);
            },

            validateConfirmation: function(value, constraints) {
                var passwordValue = this.password.get("value");
                console.debug("********" + passwordValue);

                if ( passwordValue != value)
                {
                    return false;
                }

                return true;
            },

            validatePassword: function(value, constraints) {
                var toReturn = this.verifyPassword(value, constraints);
                this.password.set("invalidMessage", this.promptMessage);
                return toReturn;
            },


            promptPassword: function(value, constraints) {
                this.set("state", "Incomplete");
                this.verifyPassword(value, constraints);
                this.password.set("message", this.promptMessage);
            },

            isValid: function() {
                return this.password.isValid() && this.passwordConfirmation.isValid();
            },

            reset: function() {
                this.password.reset();
                this.passwordConfirmation.reset();
            },

            postCreate: function() {

                on(this, "keyup", dojo.hitch(this, function() {
                    this.promptPassword(this.get("value"));
                }));

                this.password.validator = dojo.hitch(this, this.validatePassword);
                this.passwordConfirmation.validator = dojo.hitch(this, this.validateConfirmation);
            },

            templatePath: require.toUrl("uploadshield/templates/ValidatedPassword.htm")
        });
    });