define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", 
        "dijit/_WidgetsInTemplateMixin", "dojox/grid/DataGrid", 
        "dojo/store/Memory", "dojo/store/JsonRest", "dojo/store/Cache", 
        "dojo/data/ObjectStore",  "dojo/date/stamp", "dojo/date/locale",
        "dijit/Calendar", "dijit/form/DateTextBox", "dijit/form/TextBox", 
        "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang", 
        "dojo/_base/xhr", "dojo/json", "dijit/Tree", "dijit/tree/ObjectStoreModel", "dojo/store/Observable",
        "dojo/Evented", "dojo/window"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, DataGrid, Memory, JsonRest, Cache, ObjectStore, stamp,
    		locale, calendar, dateTextBox, textbox, select, button, on, lang, xhr, JSON, Tree, ObjectStoreModel, Observable,
            Evented, Window){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, Evented], {


            accountMembersReferenceStore: null,

        	accountMemberStore: null,

            accountMemberModel: null,

            /**
             * The dijit/Tree that lists out the users
             */
            accountMembersListTree: null,

            /**
             * The javascript array to store all items.
             * The filtered list will be built off this array
             */
            accountMembers: null,

        	initializeAccountReferenceStore: function(data) {
                var jsonData = [{
                    name: data.name,
                    label: data.name,
                    id: "root"
                }]

                for (var i=0; i < data.children.length; i++) {
                    jsonData.push({
                        name: data.children[i].username,
                        label: data.children[i].username,
                        id: data.children[i].username,
                        username: data.children[i].username,
                        email: data.children[i].email,
                        first_name: data.children[i].first_name,
                        last_name: data.children[i].last_name,
                        archived_time: data.children[i].archived_time,
                        creation_time: data.children[i].creation_time,
                        parent: "root"
                    });
                }

                this.accountMembersReferenceStore = new Memory({
                    data: jsonData,
                    getChildren: function(object){
                        //return object.children || [];
                        return this.query({parent: object.id});
                    }
                });
        	},


            createUniqueNewUserForTree: function() {

                var found = false;
                var counter = 0;
                while (found == false) {
                    var username = "New User";
                    if ( counter != 0) {
                        username += " (" + counter + ")";
                    }

                    if ( this.accountMembersReferenceStore.query({id: username}).length == 0) {
                        return username;
                    }
                    counter++;
                }
            },

            addNewUserToStore: function(user) {
                this.accountMembersReferenceStore.add({
                    name: user.username,
                    label: user.username,
                    id: user.username,
                    username: user.username,
                    email: user.email,
                    first_name: user.first_name,
                    last_name: user.last_name,
                    archived_time: user.archived_time,
                    creation_time: user.creation_time,
                    parent: "root"
                });
            },

            focusOnAccountMember: function(username) {
                this.accountMembersListTree.set("paths", [["root", username]]);
                var selectedNode = this.accountMembersListTree.get("selectedNode");

                if ( selectedNode != null ) {
                    Window.scrollIntoView(selectedNode.domNode);
                }
            },

            buildStoreDataFromReferenceAccountMembers: function(query) {
                var jsonData = this.accountMembersReferenceStore.query();

                console.debug("accountMembersReferenceStore")
                console.debug(jsonData);
                return jsonData;
            },


            filterTree: function(searchString, showArchivedUsers, showNewUsers) {

                //this.accountMembersReferenceStore.query().forEach(dojo.hitch(this, function(accountMember) {
                //    this.accountMemberStore.remove(accountMember.id);
                //}));

                this.accountMemberStore.query().forEach(dojo.hitch(this, function(accountMember) {
                    this.accountMemberStore.remove(accountMember.id);
                }));


                this.accountMembersReferenceStore.customFilterFunction = function(accountMember) {

                    if ( accountMember.creation_time == null && showNewUsers == true) {
                        return 1;
                    }


                    if ( accountMember.archived_time != null && showArchivedUsers == false) {
                        return 0;
                    }

                    if ( searchString == null || searchString.trim() == "") {
                        return 1;
                    }

                    //console.debug(searchString.toLowerCase());
                    if ( accountMember.id.toLowerCase().indexOf(searchString.toLowerCase()) >= 0) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                }

                this.accountMembersReferenceStore.query("customFilterFunction").forEach(dojo.hitch(this, function(accountMember) {
                    if ( accountMember.creation_time == null && accountMember.id != "root" ) {
                        accountMember.label = "<b><i>" + accountMember.username + "</i></b>";
                    }
                    else if (accountMember.id != "root") {
                        accountMember.name = accountMember.username;
                        accountMember.label = accountMember.username;
                        accountMember.parent = "root";
                    }


                    this.accountMemberStore.add(accountMember);
                }));
            },

            updateAccountMember: function(id, accountMember) {
                //check to see if the id has been updated
                console.debug("$$$$$$$$$$$$$$$$$");
                if ( id != accountMember.id) {

                    console.debug("NOT SAME UpdateAccountMember id : " + id + " accountMember:id" + accountMember.id);

                    this.accountMembersReferenceStore.remove(id);

                }

                console.debug("Puatting id : " + id + " accountMember:id" + accountMember.id);
                this.accountMembersReferenceStore.put(accountMember);

                console.debug(this.accountMembersReferenceStore);

            },

            getAccountMember: function(id) {

                console.debug("*******returning info for: " + id);
                var accountMember = this.accountMembersReferenceStore.query({id: id});

                if ( accountMember.length == 0 ) {
                    return null;
                }
                else {
                    return accountMember[0];
                }
            },

            removeAccountMember: function(id) {
                this.accountMembersReferenceStore.remove(id);

                console.debug("%%%%%%%%%%%%%%%%%%%%%");
                console.debug(this.accountMembersReferenceStore);
            },

            isAccountMemberVisible: function(id) {
                var accountMember = this.accountMemberStore.query({id: id});

                if ( accountMember.length == 0 ) {
                    return false;
                }
                else {
                    return true;
                }
            },

            isRootNode: function(accountMemberToTest) {
                if ( accountMemberToTest.id == "root") {
                    return true;
                }
                return false;
            },

        	postCreate: function() {

                var MyTreeNode = declare(Tree._TreeNode, {
                    _setLabelAttr: {node: "labelNode", type: "innerHTML"}
                });

        		xhr.get({
                    // The URL to request
                    url: "/account/members/listAccountMembers",
                    handleAs: "json",
                    //headers: { "Content-Type": "application/json"},
                    //content: filters,
                    load: dojo.hitch(this, function(result) {
                        console.debug(result);

                        this.initializeAccountReferenceStore(result);

                        var jsonData = this.buildStoreDataFromReferenceAccountMembers();

                        console.debug(jsonData);

                        this.accountMemberStore = new Memory({
                            data: jsonData,
                            getChildren: function(object){
                                //return object.children || [];
                                return this.query({parent: object.id});
                            }
                        });

                        this.accountMemberStore = new Observable(this.accountMemberStore);


                        // set up the model, assigning governmentStore, and assigning method to identify leaf nodes of tree
                        this.accountMemberModel = new ObjectStoreModel({
                            store: this.accountMemberStore,
                            query: {id: 'root'},
                            mayHaveChildren: function(item){
                                //return "children" in item;
                                return this.store.query({parent: item.id}).length != 0;
                            },
                            labelAttr: "label"
                        });

                        this.accountMembersListTree = new dijit.Tree({
                            model: this.accountMemberModel,
                            onOpenClick: true,
                            _createTreeNode: function(args){
                                return new MyTreeNode(args);
                            },
                            getIconClass: function(item, opened){
                                console.debug(item);
                                if (  !item || this.model.mayHaveChildren(item) || item.id == "root")  {
                                    return "organizationLeaf";
                                }
                                else {
                                    if ( item.creation_time == null ) {
                                        return "newUserLeaf";
                                    }
                                    else if ( item.archived_time != null ) {
                                        return "archivedUserLeaf";
                                    }
                                    else {
                                        return "userLeaf";
                                    }
                                }

                                //return (!item || this.model.mayHaveChildren(item)) ? ("organizationLeaf") : "userLeaf"
                            },
                            onClick : dojo.hitch(this, function(item) {
                                console.debug(item);
                                this.emit("userSelected", item);
                            })
                        }, "accountMembersListTreeDiv");
                        this.accountMembersListTree.startup();

                        this.emit("initializationCompleted");

                    })
                });
        	},

        	templatePath: require.toUrl("uploadshield/templates/AccountMemberList.htm")
        });
    });