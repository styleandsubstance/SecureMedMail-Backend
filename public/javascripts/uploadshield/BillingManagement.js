/**
 * Created by Sachin Doshi on 7/3/2014.
 */
define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dojox/grid/DataGrid", "dojo/store/Memory", "dojo/store/JsonRest", "dojo/store/Cache", "dojo/data/ObjectStore",  "dojo/date/stamp", "dojo/date/locale",
        "dijit/Calendar", "dijit/form/DateTextBox", "dijit/form/TextBox", "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang", "dojo/_base/xhr", "dojo/json", "dojox/widget/Standby"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, DataGrid, Memory, JsonRest, Cache, ObjectStore, stamp, locale, calendar, dateTextBox, textbox, select, button, on, lang, xhr, JSON, Standby) {
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

            formatDate: function(datum) {
                if (datum == null)
                    return '';

                var date = new Date(datum);
                return locale.format(date, {locale: 'en-us', formatLength: 'short'});
            },

            buildFilters: function() {
                var filters = {
                    startDate: (this.startDateCheckBox.checked)?this.startDateTextBox.get("value").getTime() : null,
                    endDate: this.endDateCheckBox.checked?this.endDateTextBox.get("value").getTime() : null,
                    globalSearch: this.searchCheckBox.checked?this.searchTextBox.get("value") : null,
                    transactionType: this.chargeTypeCheckBox.checked?this.chargeTypeSelectBox.get("value") : null,
                    transactionClass: this.chargeClassCheckBox.checked?this.chargeClassSelectBox.get("value") : null,
                    username: this.uploadedByCheckBox.checked?this.uploadedByUsername.get("value"): null
                }
                return filters;
            },

            setupCheckBoxes: function() {
                on(this.startDateCheckBox, "click", lang.hitch(this, function(e){
                    this.startDateTextBox.set("disabled",  !this.startDateCheckBox.checked);
                }));

                on(this.endDateCheckBox, "click", lang.hitch(this, function(e){
                    this.endDateTextBox.set("disabled", !this.endDateCheckBox.checked);
                }));

                on(this.searchCheckBox, "click", lang.hitch(this, function(e){
                    this.searchTextBox.set("disabled", !this.searchCheckBox.checked);
                }));

                on(this.chargeTypeCheckBox, "click", lang.hitch(this, function(e){
                    this.chargeTypeSelectBox.set("disabled", !this.chargeTypeCheckBox.checked);
                }));

                on(this.chargeClassCheckBox, "click", lang.hitch(this, function(e){
                    this.chargeClassSelectBox.set("disabled", !this.chargeClassCheckBox.checked);
                }));

                on(this.uploadedByCheckBox, "click", lang.hitch(this, function(e){
                    this.uploadedByUsername.setDisabled(!this.uploadedByCheckBox.checked);
                }));

            },

            setupApplyFilterButton: function() {

                on(this.applyFiltersButton, "click", lang.hitch(this, function(e){


                    if ( this.uploadedByCheckBox.checked && this.uploadedByUsername.isValid() == false) {
                        alert("Please select valid username");
                        return;
                    }


                    console.debug(this.startDateTextBox.get("value"));
                    if ( this.startDateCheckBox.checked &&
                        this.startDateTextBox.get("value") == null) {
                        alert("Please select a start date");
                        return false;
                    }

                    if ( this.endDateCheckBox.checked &&
                        this.endDateTextBox.get("value") == null ) {
                        alert("Please select an end date");
                        return false;
                    }

                    var filters = this.buildFilters();
                    this.accountTransactionsDataGridWidget.query = filters;
                    console.debug(filters);
                    this.accountTransactionsDataGridWidget._fetch();
                    this.updateTotals(filters);


                }));
            },

            updateTotals: function(filters) {

                var chargeTotalStandby = new Standby({target: this.accountTransactionsTotalsGrid});
                document.body.appendChild(chargeTotalStandby.domNode);
                chargeTotalStandby.startup();

                this.chargeTotal.innerHTML = '-';
                this.creditTotal.innerHTML = '-';
                this.pendingTotal.innerHTML = '-';

                chargeTotalStandby.show();

                xhr.get({
                    // The URL to request
                    url: "/account/billing/transactions/totals",
                    handleAs: "json",
                    //headers: { "Content-Type": "application/json"},
                    content: filters,

                    // The method that handles the request's successful result
                    // Handle the response any way you'd like!
                    load: dojo.hitch(this, function(result) {
                        this.chargeTotal.innerHTML = result.charge_total;
                        this.creditTotal.innerHTML = result.credit_total;
                        this.pendingTotal.innerHTML = result.pending_total;
                        chargeTotalStandby.hide();
                    })
                });
            },



            postCreate: function() {
                this.setupCheckBoxes();
                this.setupApplyFilterButton();


                var memoryStore = new dojo.store.Memory({idProperty: "guid"});
                var uploadFileStore = new dojo.store.JsonRest( { target: "/account/billing/transactions/store", idProperty: "transaction_time" });
                var cacheStore = new dojo.store.Cache(uploadFileStore, memoryStore);
                var objectStore = new dojo.data.ObjectStore({ objectStore: cacheStore });

                var layout = [[
                    {'name': 'Transaction Time', 'field': 'transaction_time', 'width': '80px', formatter: this.formatDate, noresize: true},
                    {'name': 'Charge Amount', 'field': 'billing_amount', 'width': '50px', styles: 'text-align: right;', noresize: true},
                    {'name': 'Credit Amount', 'field': 'credit_amount', 'width': '50px', styles: 'text-align: right;', noresize: true},
                    {'name': 'Pending Amount', 'field': 'pending_amount', 'width': '50px', styles: 'text-align: right;', noresize: true},
                    {'name': 'Type', 'field': 'charge_type', 'width': '50px', noresize: true},
                    {'name': 'Class', 'field': 'charge_class', 'width': '50px', noresize: true},
                    {'name': 'GUID', 'field': 'guid', 'width': '60px', noresize: true},
                    {'name': 'Uploaded By', 'field': 'upload_username', 'width': '60px', noresize: true},
                    {'name': 'Filename', 'field': 'filename', 'width': '120px', noresize: true},
                    {'name': 'From Balance', 'field': 'from_balance', 'width': '40px', noresize: true},
                    {'name': 'To Balance', 'field': 'to_balance', 'width': '40px', noresize: true}
                ]];


                this.accountTransactionsDataGridWidget = new DataGrid({
                    id: 'grid',
                    store: objectStore,
                    structure: layout});

                console.debug(this.accountTransactionsDataGridWidget);
                this.accountTransactionsDataGridWidget.placeAt(this.accountTransactionsDataGrid);

                this.accountTransactionsDataGridWidget.canSort = function() { return false; };
                this.accountTransactionsDataGridWidget.startup();

                this.uploadedByUsername.setDisabled(true);

                this.updateTotals(this.buildFilters());
            },

            templatePath: require.toUrl("uploadshield/templates/BillingManagement.htm")
        });
    });