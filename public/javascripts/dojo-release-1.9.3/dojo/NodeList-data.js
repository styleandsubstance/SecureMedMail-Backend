//>>built
define("dojo/NodeList-data",["./_base/kernel","./query","./_base/lang","./_base/array","./dom-attr"],function(g,k,l,n,m){var c=k.NodeList,d={},p=0,h=function(b){var a=m.get(b,"data-dojo-dataid");a||(a="pid"+p++,m.set(b,"data-dojo-dataid",a));return a},q=g._nodeData=function(b,a,c){var e=h(b),f;d[e]||(d[e]={});1==arguments.length&&(f=d[e]);"string"==typeof a?2<arguments.length?d[e][a]=c:f=d[e][a]:f=l.mixin(d[e],a);return f},r=g._removeNodeData=function(b,a){var c=h(b);d[c]&&(a?delete d[c][a]:delete d[c])};
c._gcNodeData=g._gcNodeData=function(){var b=k("[data-dojo-dataid]").map(h),a;for(a in d)0>n.indexOf(b,a)&&delete d[a]};l.extend(c,{data:c._adaptWithCondition(q,function(b){return 0===b.length||1==b.length&&"string"==typeof b[0]}),removeData:c._adaptAsForEach(r)});return c});
//# sourceMappingURL=NodeList-data.js.map