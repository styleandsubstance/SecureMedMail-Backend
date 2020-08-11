//>>built
require({cache:{"url:dijit/templates/Tree.html":'\x3cdiv role\x3d"tree"\x3e\n\t\x3cdiv class\x3d"dijitInline dijitTreeIndent" style\x3d"position: absolute; top: -9999px" data-dojo-attach-point\x3d"indentDetector"\x3e\x3c/div\x3e\n\t\x3cdiv class\x3d"dijitTreeExpando dijitTreeExpandoLoading" data-dojo-attach-point\x3d"rootLoadingIndicator"\x3e\x3c/div\x3e\n\t\x3cdiv data-dojo-attach-point\x3d"containerNode" class\x3d"dijitTreeContainer" role\x3d"presentation"\x3e\n\t\x3c/div\x3e\n\x3c/div\x3e\n',"url:dijit/templates/TreeNode.html":'\x3cdiv class\x3d"dijitTreeNode" role\x3d"presentation"\n\t\x3e\x3cdiv data-dojo-attach-point\x3d"rowNode" class\x3d"dijitTreeRow" role\x3d"presentation"\n\t\t\x3e\x3cspan data-dojo-attach-point\x3d"expandoNode" class\x3d"dijitInline dijitTreeExpando" role\x3d"presentation"\x3e\x3c/span\n\t\t\x3e\x3cspan data-dojo-attach-point\x3d"expandoNodeText" class\x3d"dijitExpandoText" role\x3d"presentation"\x3e\x3c/span\n\t\t\x3e\x3cspan data-dojo-attach-point\x3d"contentNode"\n\t\t\tclass\x3d"dijitTreeContent" role\x3d"presentation"\x3e\n\t\t\t\x3cspan role\x3d"presentation" class\x3d"dijitInline dijitIcon dijitTreeIcon" data-dojo-attach-point\x3d"iconNode"\x3e\x3c/span\n\t\t\t\x3e\x3cspan data-dojo-attach-point\x3d"labelNode,focusNode" class\x3d"dijitTreeLabel" role\x3d"treeitem" tabindex\x3d"-1" aria-selected\x3d"false"\x3e\x3c/span\x3e\n\t\t\x3c/span\n\t\x3e\x3c/div\x3e\n\t\x3cdiv data-dojo-attach-point\x3d"containerNode" class\x3d"dijitTreeNodeContainer" role\x3d"presentation" style\x3d"display: none;"\x3e\x3c/div\x3e\n\x3c/div\x3e\n'}});
define("dijit/Tree","dojo/_base/array dojo/aspect dojo/_base/connect dojo/cookie dojo/_base/declare dojo/Deferred dojo/promise/all dojo/dom dojo/dom-class dojo/dom-geometry dojo/dom-style dojo/errors/create dojo/fx dojo/has dojo/_base/kernel dojo/keys dojo/_base/lang dojo/on dojo/topic dojo/touch dojo/when ./a11yclick ./focus ./registry ./_base/manager ./_Widget ./_TemplatedMixin ./_Container ./_Contained ./_CssStateMixin ./_KeyNavMixin dojo/text!./templates/TreeNode.html dojo/text!./templates/Tree.html ./tree/TreeStoreModel ./tree/ForestStoreModel ./tree/_dndSelector dojo/query!css2".split(" "),
function(e,n,S,v,w,q,r,x,l,y,s,I,z,A,t,T,d,k,J,B,K,C,L,p,D,E,F,G,M,H,N,O,P,U,Q,R){function f(a){return d.delegate(a.promise||a,{addCallback:function(a){this.then(a)},addErrback:function(a){this.otherwise(a)}})}var u=w("dijit._TreeNode",[E,F,G,M,H],{item:null,isTreeNode:!0,label:"",_setLabelAttr:function(a){this.labelNode["html"==this.labelType?"innerHTML":"innerText"in this.labelNode?"innerText":"textContent"]=a;this._set("label",a)},labelType:"text",isExpandable:null,isExpanded:!1,state:"NotLoaded",
templateString:O,baseClass:"dijitTreeNode",cssStateNodes:{rowNode:"dijitTreeRow"},_setTooltipAttr:{node:"rowNode",type:"attribute",attribute:"title"},buildRendering:function(){this.inherited(arguments);this._setExpando();this._updateItemClasses(this.item);this.isExpandable&&this.labelNode.setAttribute("aria-expanded",this.isExpanded);this.setSelected(!1)},_setIndentAttr:function(a){var b=Math.max(a,0)*this.tree._nodePixelIndent+"px";s.set(this.domNode,"backgroundPosition",b+" 0px");s.set(this.rowNode,
this.isLeftToRight()?"paddingLeft":"paddingRight",b);e.forEach(this.getChildren(),function(b){b.set("indent",a+1)});this._set("indent",a)},markProcessing:function(){this.state="Loading";this._setExpando(!0)},unmarkProcessing:function(){this._setExpando(!1)},_updateItemClasses:function(a){var b=this.tree,c=b.model;b._v10Compat&&a===c.root&&(a=null);this._applyClassAndStyle(a,"icon","Icon");this._applyClassAndStyle(a,"label","Label");this._applyClassAndStyle(a,"row","Row");this.tree._startPaint(!0)},
_applyClassAndStyle:function(a,b,c){var h="_"+b+"Class";b+="Node";var g=this[h];this[h]=this.tree["get"+c+"Class"](a,this.isExpanded);l.replace(this[b],this[h]||"",g||"");s.set(this[b],this.tree["get"+c+"Style"](a,this.isExpanded)||{})},_updateLayout:function(){var a=this.getParent();!a||!a.rowNode||"none"==a.rowNode.style.display?l.add(this.domNode,"dijitTreeIsRoot"):l.toggle(this.domNode,"dijitTreeIsLast",!this.getNextSibling())},_setExpando:function(a){var b=["dijitTreeExpandoLoading","dijitTreeExpandoOpened",
"dijitTreeExpandoClosed","dijitTreeExpandoLeaf"];a=a?0:this.isExpandable?this.isExpanded?1:2:3;l.replace(this.expandoNode,b[a],b);this.expandoNodeText.innerHTML=["*","-","+","*"][a]},expand:function(){if(this._expandDeferred)return f(this._expandDeferred);this._collapseDeferred&&(this._collapseDeferred.cancel(),delete this._collapseDeferred);this.isExpanded=!0;this.labelNode.setAttribute("aria-expanded","true");(this.tree.showRoot||this!==this.tree.rootNode)&&this.containerNode.setAttribute("role",
"group");l.add(this.contentNode,"dijitTreeContentExpanded");this._setExpando();this._updateItemClasses(this.item);this==this.tree.rootNode&&this.tree.showRoot&&this.tree.domNode.setAttribute("aria-expanded","true");var a=z.wipeIn({node:this.containerNode,duration:D.defaultDuration}),b=this._expandDeferred=new q(function(){a.stop()});n.after(a,"onEnd",function(){b.resolve(!0)},!0);a.play();return f(b)},collapse:function(){if(this._collapseDeferred)return f(this._collapseDeferred);this._expandDeferred&&
(this._expandDeferred.cancel(),delete this._expandDeferred);this.isExpanded=!1;this.labelNode.setAttribute("aria-expanded","false");this==this.tree.rootNode&&this.tree.showRoot&&this.tree.domNode.setAttribute("aria-expanded","false");l.remove(this.contentNode,"dijitTreeContentExpanded");this._setExpando();this._updateItemClasses(this.item);var a=z.wipeOut({node:this.containerNode,duration:D.defaultDuration}),b=this._collapseDeferred=new q(function(){a.stop()});n.after(a,"onEnd",function(){b.resolve(!0)},
!0);a.play();return f(b)},indent:0,setChildItems:function(a){var b=this.tree,c=b.model,h=[],g=this.getChildren();e.forEach(g,function(a){G.prototype.removeChild.call(this,a)},this);this.defer(function(){e.forEach(g,function(a){if(!a._destroyed&&!a.getParent()){b.dndController.removeTreeNode(a);var h=function(a){var g=c.getIdentity(a.item),d=b._itemNodesMap[g];1==d.length?delete b._itemNodesMap[g]:(g=e.indexOf(d,a),-1!=g&&d.splice(g,1));e.forEach(a.getChildren(),h)};h(a);if(b.persist){var g=e.map(a.getTreePath(),
function(a){return b.model.getIdentity(a)}).join("/"),d;for(d in b._openedNodes)d.substr(0,g.length)==g&&delete b._openedNodes[d];b._saveExpandedNodes()}a.destroyRecursive()}})});this.state="Loaded";a&&0<a.length?(this.isExpandable=!0,e.forEach(a,function(a){var g=c.getIdentity(a),d=b._itemNodesMap[g],e;if(d)for(var f=0;f<d.length;f++)if(d[f]&&!d[f].getParent()){e=d[f];e.set("indent",this.indent+1);break}e||(e=this.tree._createTreeNode({item:a,tree:b,isExpandable:c.mayHaveChildren(a),label:b.getLabel(a),
labelType:b.model&&b.model.labelType||"text",tooltip:b.getTooltip(a),ownerDocument:b.ownerDocument,dir:b.dir,lang:b.lang,textDir:b.textDir,indent:this.indent+1}),d?d.push(e):b._itemNodesMap[g]=[e]);this.addChild(e);(this.tree.autoExpand||this.tree._state(e))&&h.push(b._expandNode(e))},this),e.forEach(this.getChildren(),function(a){a._updateLayout()})):this.isExpandable=!1;this._setExpando&&this._setExpando(!1);this._updateItemClasses(this.item);a=r(h);this.tree._startPaint(a);return f(a)},getTreePath:function(){for(var a=
this,b=[];a&&a!==this.tree.rootNode;)b.unshift(a.item),a=a.getParent();b.unshift(this.tree.rootNode.item);return b},getIdentity:function(){return this.tree.model.getIdentity(this.item)},removeChild:function(a){this.inherited(arguments);var b=this.getChildren();0==b.length&&(this.isExpandable=!1,this.collapse());e.forEach(b,function(a){a._updateLayout()})},makeExpandable:function(){this.isExpandable=!0;this._setExpando(!1)},setSelected:function(a){this.labelNode.setAttribute("aria-selected",a?"true":
"false");l.toggle(this.rowNode,"dijitTreeRowSelected",a)},focus:function(){L.focus(this.focusNode)}});A("dojo-bidi")&&u.extend({_setTextDirAttr:function(a){if(a&&(this.textDir!=a||!this._created))this._set("textDir",a),this.applyTextDir(this.labelNode),e.forEach(this.getChildren(),function(b){b.set("textDir",a)},this)}});var m=w("dijit.Tree",[E,N,F,H],{baseClass:"dijitTree",store:null,model:null,query:null,label:"",showRoot:!0,childrenAttr:["children"],paths:[],path:[],selectedItems:null,selectedItem:null,
openOnClick:!1,openOnDblClick:!1,templateString:P,persist:!1,autoExpand:!1,dndController:R,dndParams:"onDndDrop itemCreator onDndCancel checkAcceptance checkItemAcceptance dragThreshold betweenThreshold".split(" "),onDndDrop:null,itemCreator:null,onDndCancel:null,checkAcceptance:null,checkItemAcceptance:null,dragThreshold:5,betweenThreshold:0,_nodePixelIndent:19,_publish:function(a,b){J.publish(this.id,d.mixin({tree:this,event:a},b||{}))},postMixInProperties:function(){this.tree=this;this.autoExpand&&
(this.persist=!1);this._itemNodesMap={};!this.cookieName&&this.id&&(this.cookieName=this.id+"SaveStateCookie");this.expandChildrenDeferred=new q;this.pendingCommandsPromise=this.expandChildrenDeferred.promise;this.inherited(arguments)},postCreate:function(){this._initState();var a=this;this.own(k(this.containerNode,k.selector(".dijitTreeNode",B.enter),function(b){a._onNodeMouseEnter(p.byNode(this),b)}),k(this.containerNode,k.selector(".dijitTreeNode",B.leave),function(b){a._onNodeMouseLeave(p.byNode(this),
b)}),k(this.containerNode,k.selector(".dijitTreeRow",C.press),function(b){a._onNodePress(p.getEnclosingWidget(this),b)}),k(this.containerNode,k.selector(".dijitTreeRow",C),function(b){a._onClick(p.getEnclosingWidget(this),b)}),k(this.containerNode,k.selector(".dijitTreeRow","dblclick"),function(b){a._onDblClick(p.getEnclosingWidget(this),b)}));this.model||this._store2model();this.own(n.after(this.model,"onChange",d.hitch(this,"_onItemChange"),!0),n.after(this.model,"onChildrenChange",d.hitch(this,
"_onItemChildrenChange"),!0),n.after(this.model,"onDelete",d.hitch(this,"_onItemDelete"),!0));this.inherited(arguments);if(this.dndController){d.isString(this.dndController)&&(this.dndController=d.getObject(this.dndController));for(var b={},c=0;c<this.dndParams.length;c++)this[this.dndParams[c]]&&(b[this.dndParams[c]]=this[this.dndParams[c]]);this.dndController=new this.dndController(this,b)}this._load();this.onLoadDeferred=f(this.pendingCommandsPromise);this.onLoadDeferred.then(d.hitch(this,"onLoad"))},
_store2model:function(){this._v10Compat=!0;t.deprecated("Tree: from version 2.0, should specify a model object rather than a store/query");var a={id:this.id+"_ForestStoreModel",store:this.store,query:this.query,childrenAttrs:this.childrenAttr};this.params.mayHaveChildren&&(a.mayHaveChildren=d.hitch(this,"mayHaveChildren"));this.params.getItemChildren&&(a.getChildren=d.hitch(this,function(a,c,h){this.getItemChildren(this._v10Compat&&a===this.model.root?null:a,c,h)}));this.model=new Q(a);this.showRoot=
Boolean(this.label)},onLoad:function(){},_load:function(){this.model.getRoot(d.hitch(this,function(a){var b=this.rootNode=this.tree._createTreeNode({item:a,tree:this,isExpandable:!0,label:this.label||this.getLabel(a),labelType:this.model.labelType||"text",textDir:this.textDir,indent:this.showRoot?0:-1});this.showRoot?(this.domNode.setAttribute("aria-multiselectable",!this.dndController.singular),this.rootLoadingIndicator.style.display="none"):(b.rowNode.style.display="none",this.domNode.setAttribute("role",
"presentation"),this.domNode.removeAttribute("aria-expanded"),this.domNode.removeAttribute("aria-multiselectable"),this["aria-label"]?(b.containerNode.setAttribute("aria-label",this["aria-label"]),this.domNode.removeAttribute("aria-label")):this["aria-labelledby"]&&(b.containerNode.setAttribute("aria-labelledby",this["aria-labelledby"]),this.domNode.removeAttribute("aria-labelledby")),b.labelNode.setAttribute("role","presentation"),b.containerNode.setAttribute("role","tree"),b.containerNode.setAttribute("aria-expanded",
"true"),b.containerNode.setAttribute("aria-multiselectable",!this.dndController.singular));this.containerNode.appendChild(b.domNode);a=this.model.getIdentity(a);this._itemNodesMap[a]?this._itemNodesMap[a].push(b):this._itemNodesMap[a]=[b];b._updateLayout();this._expandNode(b).then(d.hitch(this,function(){this.rootLoadingIndicator.style.display="none";this.expandChildrenDeferred.resolve(!0)}))}),d.hitch(this,function(a){console.error(this,": error loading root: ",a)}))},getNodesByItem:function(a){if(!a)return[];
a=d.isString(a)?a:this.model.getIdentity(a);return[].concat(this._itemNodesMap[a])},_setSelectedItemAttr:function(a){this.set("selectedItems",[a])},_setSelectedItemsAttr:function(a){var b=this;return this.pendingCommandsPromise=this.pendingCommandsPromise.always(d.hitch(this,function(){var c=e.map(a,function(a){return!a||d.isString(a)?a:b.model.getIdentity(a)}),h=[];e.forEach(c,function(a){h=h.concat(b._itemNodesMap[a]||[])});this.set("selectedNodes",h)}))},_setPathAttr:function(a){return a.length?
f(this.set("paths",[a]).then(function(a){return a[0]})):f(this.set("paths",[]).then(function(a){return a[0]}))},_setPathsAttr:function(a){function b(a,g){var d=a.shift(),f=e.filter(g,function(a){return a.getIdentity()==d})[0];if(f)return a.length?c._expandNode(f).then(function(){return b(a,f.getChildren())}):f;throw new m.PathError("Could not expand path at "+d);}var c=this;return f(this.pendingCommandsPromise=this.pendingCommandsPromise.always(function(){return r(e.map(a,function(a){a=e.map(a,function(a){return d.isString(a)?
a:c.model.getIdentity(a)});if(a.length)return b(a,[c.rootNode]);throw new m.PathError("Empty path");}))}).then(function(a){c.set("selectedNodes",a);return c.paths}))},_setSelectedNodeAttr:function(a){this.set("selectedNodes",[a])},_setSelectedNodesAttr:function(a){this.dndController.setSelection(a)},expandAll:function(){function a(c){return b._expandNode(c).then(function(){var b=e.filter(c.getChildren()||[],function(a){return a.isExpandable});return r(e.map(b,a))})}var b=this;return f(a(this.rootNode))},
collapseAll:function(){function a(c){var d=e.filter(c.getChildren()||[],function(a){return a.isExpandable}),d=r(e.map(d,a));return!c.isExpanded||c==b.rootNode&&!b.showRoot?d:d.then(function(){return b._collapseNode(c)})}var b=this;return f(a(this.rootNode))},mayHaveChildren:function(){},getItemChildren:function(){},getLabel:function(a){return this.model.getLabel(a)},getIconClass:function(a,b){return!a||this.model.mayHaveChildren(a)?b?"dijitFolderOpened":"dijitFolderClosed":"dijitLeaf"},getLabelClass:function(){},
getRowClass:function(){},getIconStyle:function(){},getLabelStyle:function(){},getRowStyle:function(){},getTooltip:function(){return""},_onDownArrow:function(a,b){var c=this._getNext(b);c&&c.isTreeNode&&this.focusNode(c)},_onUpArrow:function(a,b){var c=b.getPreviousSibling();if(c)for(b=c;b.isExpandable&&b.isExpanded&&b.hasChildren();)c=b.getChildren(),b=c[c.length-1];else if(c=b.getParent(),this.showRoot||c!==this.rootNode)b=c;b&&b.isTreeNode&&this.focusNode(b)},_onRightArrow:function(a,b){b.isExpandable&&
!b.isExpanded?this._expandNode(b):b.hasChildren()&&(b=b.getChildren()[0])&&b.isTreeNode&&this.focusNode(b)},_onLeftArrow:function(a,b){if(b.isExpandable&&b.isExpanded)this._collapseNode(b);else{var c=b.getParent();c&&(c.isTreeNode&&(this.showRoot||c!==this.rootNode))&&this.focusNode(c)}},focusLastChild:function(){var a=this._getLast();a&&a.isTreeNode&&this.focusNode(a)},_getFirst:function(){return this.showRoot?this.rootNode:this.rootNode.getChildren()[0]},_getLast:function(){for(var a=this.rootNode;a.isExpanded;){var b=
a.getChildren();if(!b.length)break;a=b[b.length-1]}return a},_getNext:function(a){if(a.isExpandable&&a.isExpanded&&a.hasChildren())return a.getChildren()[0];for(;a&&a.isTreeNode;){var b=a.getNextSibling();if(b)return b;a=a.getParent()}return null},childSelector:".dijitTreeRow",isExpandoNode:function(a,b){return x.isDescendant(a,b.expandoNode)||x.isDescendant(a,b.expandoNodeText)},_onNodePress:function(a,b){a.focus()},__click:function(a,b,c,d){var e=this.isExpandoNode(b.target,a);a.isExpandable&&(c||
e)?this._onExpandoClick({node:a}):(this._publish("execute",{item:a.item,node:a,evt:b}),this[d](a.item,a,b),this.focusNode(a));b.stopPropagation();b.preventDefault()},_onClick:function(a,b){this.__click(a,b,this.openOnClick,"onClick")},_onDblClick:function(a,b){this.__click(a,b,this.openOnDblClick,"onDblClick")},_onExpandoClick:function(a){a=a.node;this.focusNode(a);a.isExpanded?this._collapseNode(a):this._expandNode(a)},onClick:function(){},onDblClick:function(){},onOpen:function(){},onClose:function(){},
_getNextNode:function(a){t.deprecated(this.declaredClass+"::_getNextNode(node) is deprecated. Use _getNext(node) instead.","","2.0");return this._getNext(a)},_getRootOrFirstNode:function(){t.deprecated(this.declaredClass+"::_getRootOrFirstNode() is deprecated. Use _getFirst() instead.","","2.0");return this._getFirst()},_collapseNode:function(a){a._expandNodeDeferred&&delete a._expandNodeDeferred;if("Loading"!=a.state&&a.isExpanded){var b=a.collapse();this.onClose(a.item,a);this._state(a,!1);this._startPaint(b);
return b}},_expandNode:function(a){if(a._expandNodeDeferred)return a._expandNodeDeferred;var b=this.model,c=a.item,e=this;a._loadDeferred||(a.markProcessing(),a._loadDeferred=new q,b.getChildren(c,function(b){a.unmarkProcessing();a.setChildItems(b).then(function(){a._loadDeferred.resolve(b)})},function(b){console.error(e,": error loading "+a.label+" children: ",b);a._loadDeferred.reject(b)}));b=a._loadDeferred.then(d.hitch(this,function(){var b=a.expand();this.onOpen(a.item,a);this._state(a,!0);return b}));
this._startPaint(b);return b},focusNode:function(a){this.focusChild(a)},_onNodeMouseEnter:function(){},_onNodeMouseLeave:function(){},_onItemChange:function(a){var b=this.model.getIdentity(a);if(b=this._itemNodesMap[b]){var c=this.getLabel(a),d=this.getTooltip(a);e.forEach(b,function(b){b.set({item:a,label:c,tooltip:d});b._updateItemClasses(a)})}},_onItemChildrenChange:function(a,b){var c=this.model.getIdentity(a);(c=this._itemNodesMap[c])&&e.forEach(c,function(a){a.setChildItems(b)})},_onItemDelete:function(a){a=
this.model.getIdentity(a);var b=this._itemNodesMap[a];b&&(e.forEach(b,function(a){this.dndController.removeTreeNode(a);var b=a.getParent();b&&b.removeChild(a);a.destroyRecursive()},this),delete this._itemNodesMap[a])},_initState:function(){this._openedNodes={};if(this.persist&&this.cookieName){var a=v(this.cookieName);a&&e.forEach(a.split(","),function(a){this._openedNodes[a]=!0},this)}},_state:function(a,b){if(!this.persist)return!1;var c=e.map(a.getTreePath(),function(a){return this.model.getIdentity(a)},
this).join("/");if(1===arguments.length)return this._openedNodes[c];b?this._openedNodes[c]=!0:delete this._openedNodes[c];this._saveExpandedNodes()},_saveExpandedNodes:function(){if(this.persist&&this.cookieName){var a=[],b;for(b in this._openedNodes)a.push(b);v(this.cookieName,a.join(","),{expires:365})}},destroy:function(){this._curSearch&&(this._curSearch.timer.remove(),delete this._curSearch);this.rootNode&&this.rootNode.destroyRecursive();this.dndController&&!d.isString(this.dndController)&&
this.dndController.destroy();this.rootNode=null;this.inherited(arguments)},destroyRecursive:function(){this.destroy()},resize:function(a){a&&y.setMarginBox(this.domNode,a);this._nodePixelIndent=y.position(this.tree.indentDetector).w||this._nodePixelIndent;this.expandChildrenDeferred.then(d.hitch(this,function(){this.rootNode.set("indent",this.showRoot?0:-1);this._adjustWidths()}))},_outstandingPaintOperations:0,_startPaint:function(a){this._outstandingPaintOperations++;this._adjustWidthsTimer&&(this._adjustWidthsTimer.remove(),
delete this._adjustWidthsTimer);var b=d.hitch(this,function(){this._outstandingPaintOperations--;0>=this._outstandingPaintOperations&&(!this._adjustWidthsTimer&&this._started)&&(this._adjustWidthsTimer=this.defer("_adjustWidths"))});K(a,b,b)},_adjustWidths:function(){this._adjustWidthsTimer&&(this._adjustWidthsTimer.remove(),delete this._adjustWidthsTimer);this.containerNode.style.width="auto";this.containerNode.style.width=this.domNode.scrollWidth>this.domNode.offsetWidth?"auto":"100%"},_createTreeNode:function(a){return new u(a)},
focus:function(){this.lastFocusedChild?this.focusNode(this.lastFocusedChild):this.focusFirstChild()}});A("dojo-bidi")&&m.extend({_setTextDirAttr:function(a){a&&this.textDir!=a&&(this._set("textDir",a),this.rootNode.set("textDir",a))}});m.PathError=I("TreePathError");m._TreeNode=u;return m});
//# sourceMappingURL=Tree.js.map