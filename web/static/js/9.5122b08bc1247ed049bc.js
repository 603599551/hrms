webpackJsonp([9],{"4b5g":function(e,t,r){var a=r("cxo/");"string"==typeof a&&(a=[[e.i,a,""]]),a.locals&&(e.exports=a.locals);r("rjj0")("c6b064ba",a,!0,{})},"73OC":function(e,t,r){var a=r("fCRE");"string"==typeof a&&(a=[[e.i,a,""]]),a.locals&&(e.exports=a.locals);r("rjj0")("2dc2ebd3",a,!0,{})},"Fyd/":function(e,t,r){(e.exports=r("FZ+f")(!1)).push([e.i,"",""])},R8Mj:function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=r("Xxa5"),s=r.n(a),o=r("exGp"),n=r.n(o),i=r("Dd8w"),l=r.n(i),c=r("gyMJ"),d=r("NYxO"),u=r("ecGJ"),f={name:"app-show-score",props:["recordId"],data:function(){return{form:{names:"",date:"",type_text:"",money:"",desc:""}}},methods:{getFormInfo:function(e,t){var r=this;return n()(s.a.mark(function a(){var o;return s.a.wrap(function(a){for(;;)switch(a.prev=a.next){case 0:return a.next=2,e();case 2:1==(o=a.sent).code?r[t]=o.data||[]:r.$message.error(o.message);case 4:case"end":return a.stop()}},a,r)}))()}},created:function(){var e=this;this.getFormInfo(n()(s.a.mark(function t(){return s.a.wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.abrupt("return",Object(c.Q)({id:e.recordId}));case 1:case"end":return t.stop()}},t,e)})),"form")}},m={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticStyle:{width:"70%"}},[r("el-form",{ref:"form",attrs:{model:e.form,"label-width":"100px",size:"small"}},[r("el-form-item",{attrs:{label:"员工："}},[r("span",{staticClass:"form-record-show"},[e._v(e._s(e.form.names))])]),e._v(" "),r("el-form-item",{attrs:{label:"日期："}},[r("span",{staticClass:"form-record-show"},[e._v(e._s(e.form.date))])]),e._v(" "),r("el-form-item",{attrs:{label:"类别："}},[r("span",{staticClass:"form-record-show"},[e._v(e._s(e.form.type_text))])]),e._v(" "),r("el-form-item",{attrs:{label:"金额："}},[r("span",{staticClass:"form-record-show"},[e._v(e._s(e.form.money))])]),e._v(" "),r("el-form-item",{attrs:{label:"说明："}},[r("span",{staticClass:"form-record-show"},[e._v(e._s(e.form.desc))])])],1)],1)},staticRenderFns:[]};var p=r("VU/8")(f,m,!1,function(e){r("73OC")},null,null).exports,v={name:"app-mod-score",props:["recordId"],data:function(){return{form:{id:"",names:"",date:"",type:"",money:"",desc:""},rules:{type:[{required:!0,message:"请选择类别",trigger:"change"}],money:[{required:!0,message:"请输入金额",trigger:"blur"}],desc:[{required:!0,message:"请输入说明",trigger:"blur"}]}}},computed:l()({},Object(d.c)("stateChange",["btnLoading"]),Object(d.c)("dict",["scoreTypeList"])),methods:l()({},Object(d.b)("dict",["createScoreTypeList"]),{getFormInfo:function(e,t){var r=this;return n()(s.a.mark(function a(){var o;return s.a.wrap(function(a){for(;;)switch(a.prev=a.next){case 0:return a.next=2,e();case 2:1==(o=a.sent).code?r[t]=o.data||[]:r.$message.error(o.message);case 4:case"end":return a.stop()}},a,r)}))()},saveRecord:function(){var e=this;return n()(s.a.mark(function t(){var r;return s.a.wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.next=2,Object(c.Z)(e.form);case 2:1==(r=t.sent).code?e.closePanle():e.$message.error(r.message);case 4:case"end":return t.stop()}},t,e)}))()},submitForm:function(){var e=this;this.$refs.form.validate(function(t){t?e.saveRecord():console.log("error submit!")})},resetForm:function(){this.$refs.form.resetFields()},closePanle:function(){this.$emit("reloadEvent","reload")}}),created:function(){var e=this;this.getFormInfo(n()(s.a.mark(function t(){return s.a.wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.abrupt("return",Object(c.Q)({id:e.recordId}));case 1:case"end":return t.stop()}},t,e)})),"form"),this.createScoreTypeList()}},h={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticStyle:{width:"70%"}},[r("el-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-width":"100px",size:"small"}},[r("el-form-item",{attrs:{label:"员工"}},[r("el-input",{attrs:{disabled:""},model:{value:e.form.names,callback:function(t){e.$set(e.form,"names",t)},expression:"form.names"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"日期"}},[r("el-date-picker",{attrs:{type:"date",disabled:"",format:"yyyy 年 MM 月 dd 日","value-format":"yyyy-MM-dd"},model:{value:e.form.date,callback:function(t){e.$set(e.form,"date",t)},expression:"form.date"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"类别",prop:"type"}},[r("el-select",{attrs:{clearable:"",placeholder:"请选类别"},model:{value:e.form.type,callback:function(t){e.$set(e.form,"type",t)},expression:"form.type"}},e._l(e.scoreTypeList,function(e,t){return r("el-option",{key:t,attrs:{label:e.name,value:e.value}})}))],1),e._v(" "),r("el-form-item",{attrs:{label:"金额",prop:"money"}},[r("el-input",{attrs:{type:"number",placeholder:"保留两位小数"},model:{value:e.form.money,callback:function(t){e.$set(e.form,"money",t)},expression:"form.money"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"说明",prop:"desc"}},[r("el-input",{attrs:{type:"textarea",rows:4,clearable:"",placeholder:"请输说明..."},model:{value:e.form.desc,callback:function(t){e.$set(e.form,"desc",t)},expression:"form.desc"}})],1),e._v(" "),r("el-form-item",[r("el-button",{attrs:{type:"primary",loading:e.btnLoading},on:{click:e.submitForm}},[e._v("提交")]),e._v(" "),r("el-button",{on:{click:e.resetForm}},[e._v("重置")])],1)],1)],1)},staticRenderFns:[]};var b=r("VU/8")(v,h,!1,function(e){r("e7xR")},null,null).exports,g={name:"app-score-list",data:function(){return{search:{store_id:"",start_date:"",end_date:"",keyword:""},list:[],loading:!1,curPageIndex:1,dialog:{showVisible:!1,modVisible:!1},recordId:""}},computed:l()({},Object(d.c)("dict",["deptList","transferInList"])),methods:l()({},Object(d.b)("dict",["createDeptList","createTransferInList"]),{recordHandler:function(e,t){this.dialog[t]=!0,this.recordId=e},getStaffScoreInfo:function(e,t){var r=this;return n()(s.a.mark(function a(){var o;return s.a.wrap(function(a){for(;;)switch(a.prev=a.next){case 0:return e=e>0?Number(e):r.curPageIndex,r.loading=!0,a.next=4,Object(c.P)(l()({pageNum:e,pageSize:10},r.search));case 4:1==(o=a.sent).code?(r.list=o.data.list,r.list.total=o.data.totalRow||1,t&&t()):r.$message.error(o.data.message),r.loading=!1;case 7:case"end":return a.stop()}},a,r)}))()},deleteHandler:function(e){var t=this;this.$confirm("确认删除吗?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(n()(s.a.mark(function r(){var a;return s.a.wrap(function(r){for(;;)switch(r.prev=r.next){case 0:return r.next=2,Object(c.l)({id:e});case 2:1==(a=r.sent).code?(t.getStaffScoreInfo(t.curPageIndex),t.$message.success(a.message)):t.$message.error(a.message);case 4:case"end":return r.stop()}},r,t)}))).catch(function(){})},searchHandle:function(){this.getStaffScoreInfo(1)},handleCurrentChange:function(e){this.curPageIndex=e,this.getStaffScoreInfo(e)},reloadGetData:function(e){if("reload"==e){for(var t in this.dialog)this.dialog[t]=!1;this.getStaffScoreInfo(this.curPageIndex)}}}),created:function(){this.getStaffScoreInfo(),this.createDeptList(),this.createTransferInList()},components:{AppDialog:u.a,AppShowScore:p,AppModScore:b}},y={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",[r("nav",{staticClass:"app-location-wrapper"},[r("el-breadcrumb",{staticClass:"fl",attrs:{separator:"/"}},[r("el-breadcrumb-item",{attrs:{to:{path:"/performance"}}},[e._v("绩效管理")]),e._v(" "),r("el-breadcrumb-item",[e._v("绩效考核")]),e._v(" "),r("el-breadcrumb-item",[e._v("绩效列表")])],1)],1),e._v(" "),r("div",{staticClass:"component-top"},[r("div",{staticClass:"search-title fl"},[e._v("门店：")]),e._v(" "),r("el-select",{staticClass:"fl",staticStyle:{width:"160px","margin-right":"10px"},attrs:{size:"small",clearable:"",placeholder:"门店"},on:{change:e.searchHandle},model:{value:e.search.store_id,callback:function(t){e.$set(e.search,"store_id",t)},expression:"search.store_id"}},e._l(e.deptList,function(e,t){return r("el-option",{key:t,attrs:{label:e.name,value:e.value}})})),e._v(" "),r("div",{staticClass:"search-title fl"},[e._v("开始日期：")]),e._v(" "),r("el-date-picker",{staticClass:"fl",staticStyle:{width:"160px","margin-right":"10px"},attrs:{size:"small",type:"date",placeholder:"开始日期",format:"yyyy 年 MM 月 dd 日","value-format":"yyyy-MM-dd"},on:{change:e.searchHandle},model:{value:e.search.start_date,callback:function(t){e.$set(e.search,"start_date",t)},expression:"search.start_date"}}),e._v(" "),r("div",{staticClass:"search-title fl"},[e._v("结束日期：")]),e._v(" "),r("el-date-picker",{staticClass:"fl",staticStyle:{width:"160px","margin-right":"10px"},attrs:{size:"small",type:"date",placeholder:"结束日期",format:"yyyy 年 MM 月 dd 日","value-format":"yyyy-MM-dd"},on:{change:e.searchHandle},model:{value:e.search.end_date,callback:function(t){e.$set(e.search,"end_date",t)},expression:"search.end_date"}}),e._v(" "),r("el-input",{staticClass:"fl",staticStyle:{width:"160px"},attrs:{size:"small",placeholder:"姓名/拼音","prefix-icon":"el-icon-search",clearable:""},nativeOn:{keyup:function(t){return"button"in t||!e._k(t.keyCode,"enter",13,t.key,"Enter")?e.searchHandle(t):null}},model:{value:e.search.keyword,callback:function(t){e.$set(e.search,"keyword",t)},expression:"search.keyword"}})],1),e._v(" "),r("div",{staticClass:"component-main"},[r("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],attrs:{size:"small",data:e.list,stripe:"",border:""}},[r("el-table-column",{attrs:{label:"门店名称","min-width":"200"},scopedSlots:e._u([{key:"default",fn:function(t){return[r("span",{style:{color:t.row.store_color}},[e._v(e._s(t.row.store_name))])]}}])}),e._v(" "),r("el-table-column",{attrs:{prop:"date",label:"日期"}}),e._v(" "),r("el-table-column",{attrs:{prop:"type",label:"奖罚"}}),e._v(" "),r("el-table-column",{attrs:{prop:"name",label:"姓名"}}),e._v(" "),r("el-table-column",{attrs:{prop:"money",label:"金额"}}),e._v(" "),r("el-table-column",{attrs:{label:"操作",width:"230"},scopedSlots:e._u([{key:"default",fn:function(t){return[r("el-button",{attrs:{size:"mini"},on:{click:function(r){r.stopPropagation(),e.recordHandler(t.row.id,"showVisible")}}},[e._v("\r\n                        查看\r\n                    ")]),r("el-button",{attrs:{type:"primary",size:"mini"},on:{click:function(r){r.stopPropagation(),e.recordHandler(t.row.id,"modVisible")}}},[e._v("\r\n                        修改\r\n                    ")]),r("el-button",{attrs:{type:"danger",size:"mini"},on:{click:function(r){r.stopPropagation(),e.deleteHandler(t.row.id)}}},[e._v("\r\n                        删除\r\n                    ")])]}}])})],1),e._v(" "),r("el-pagination",{staticClass:"pagination",attrs:{background:"",layout:"prev, pager, next, jumper",total:e.list.total},on:{"current-change":e.handleCurrentChange}})],1),e._v(" "),r("app-dialog",{attrs:{title:"查看奖罚信息",visible:e.dialog.showVisible},on:{"update:visible":function(t){e.$set(e.dialog,"showVisible",t)}}},[r("app-show-score",{attrs:{"record-id":e.recordId}})],1),e._v(" "),r("app-dialog",{attrs:{title:"修改奖罚信息",visible:e.dialog.modVisible},on:{"update:visible":function(t){e.$set(e.dialog,"modVisible",t)}}},[r("app-mod-score",{attrs:{"record-id":e.recordId}})],1)],1)},staticRenderFns:[]};var _=r("VU/8")(g,y,!1,function(e){r("4b5g")},null,null);t.default=_.exports},"cxo/":function(e,t,r){(e.exports=r("FZ+f")(!1)).push([e.i,"",""])},e7xR:function(e,t,r){var a=r("Fyd/");"string"==typeof a&&(a=[[e.i,a,""]]),a.locals&&(e.exports=a.locals);r("rjj0")("381754f0",a,!0,{})},fCRE:function(e,t,r){(e.exports=r("FZ+f")(!1)).push([e.i,"",""])}});
//# sourceMappingURL=9.5122b08bc1247ed049bc.js.map