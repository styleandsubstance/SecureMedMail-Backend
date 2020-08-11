/**
 * Created by sdoshi on 8/16/2014.
 */
define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin",
        "dijit/form/ValidationTextBox", "dojo/on", "dojo/_base/lang",
        "uploadshield/util/ajax", "dojo/dom-class"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, validatedTextbox, on, lang, uploadshield, domClass){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, validatedTextbox], {

            usernameIsAvailable: true,

            promptMessage: null,

            verifyUsername: function(value) {
                if ( value.length < 5)
                {
                    this.promptMessage = "Username must be at least 5 characters";
                    return false;
                }

                if ( value.length > 32 )
                {
                    this.promptMessage = "Username cannot be longer than 32 characters";
                    return false;
                }

                if (!value.match(/^[0-9a-z]+$/))
                {
                    this.promptMessage = "Usernames can only contain alpha numberic characters. (A-Z and 0-9)";
                    return false;
                }

                //now check to make sure username doesn't start with a number
                if (!value.match(/^[a-z]/))
                {
                    this.promptMessage = "Usernames must begin with an alphabetic character [a-z]";
                    return false;
                }

                //now check to make sure that username is available
                uploadshield.jsonPostSync(
                    "/account/members/verifyUsernameAvailibility",
                    {username: value},
                    dojo.hitch(this, function(response) {
                        this.usernameIsAvailable = response.success;
                    })
                );

                if (this.usernameIsAvailable == false) {
                    this.promptMessage = "This username is unavailable";
                    return false;
                }

                this.promptMessage = null;
                return true;
            },


            validateUsername: function(value, constraints) {
                var toReturn = this.verifyUsername(value, constraints);
                this.set("invalidMessage", this.promptMessage);
                return toReturn;
            },


            promptUsername: function(value, constraints) {
                this.set("state", "Incomplete");
                this.verifyUsername(value, constraints);
                this.set("message", this.promptMessage);
            },


            postCreate: function() {
                on(this, "keyup", dojo.hitch(this, function() {
                    this.promptUsername(this.get("value"));
                }));

                this.validator = this.validateUsername;
            }
        });
    });