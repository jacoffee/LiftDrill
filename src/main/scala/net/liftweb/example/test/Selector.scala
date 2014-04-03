package net.liftweb.example.test

import org.jsoup.Jsoup

object Selector extends App {
	val bb = """
		<div id="sendtimepadding" style="border-bottom:1px solid #fff;min-width:650px;_zoom:1;" class="readmailinfo">
  <div style="float:right;width:179px!important;width:180px;margin:16px 12px 0 0; margin:14px 14px 0 0\9;filter:none;" id="rightArea">
   <div onclick="changeTab('AddrTab');" style="_width:88px;" class="cptab cpslt" onmouseout="if(this.className == 'cptab toolbg')this.className='cptab'" onmouseover="if(this.className != 'cptab cpslt')this.className='cptab toolbg'" id="addr_cmd">
    <a hidefocus="">通讯录</a>
   </div>
   <div onclick="useStationery_new();" style="margin-left:89px;margin-left:90px\9;border-left-width:1px;border-left-width:0px\9;" class="cptab" onmouseout="if(this.className == 'cptab toolbg')this.className='cptab'" onmouseover="if(this.className != 'cptab cpslt')this.className='cptab toolbg'" id="stationery_cmd">
    <a hidefocus="">信&nbsp;&nbsp;纸</a>
   </div>
   <div class="grptitle_tab_" style="height:25px;filter:none;"></div>
   <div style="" class="addrtab cpright" id="AddrTab">
    <div id="quickaddr_div"></div>
   </div>
   <div style="position:relative;width:177px!important;width:178px;_width:179px;height:369px;display:none;" class="cpright" id="stationery_div"> 
    <input value="" name="stationeryCount" id="stationeryCount" type="hidden"> 
    <div style="height:27px;margin:6px 11px 0;"> 
     <div style="float:right;font-size:14px;margin-top:4px" id="page_span"> 
      <a class="paper_controller_btn" disabled="true" href="javascript:;" id="paper_prev_b"> <img src="http://rescdn.qqmail.com/zh_CN/htmledition/images/spacer104474.gif" id="paper_prev_b_img"> </a>
      <span id="pageid"></span>
      <a class="paper_controller_btn" href="javascript:;" id="paper_next_b"> <img src="http://rescdn.qqmail.com/zh_CN/htmledition/images/spacer104474.gif" id="paper_next_b_img"> </a> 
     </div> 
    </div> 
    <ul id="show_paper"></ul> 
    <div id="new_stationery" style="clear:both; position:absolute; bottom:2px; right:9px; width:155px;"></div> 
   </div>
  </div>
  <div style="float:right;margin:200px 3px 0 6px!important;margin:200px 5px 0 3px;height:220px;x_height:120px;">
   <a onclick="hideRightArea(false)" style="height:200px;font:bold 12px 宋体;text-decoration:none;color:#585858" id="rightAreaBtn"><input class="prefd" type="button"></a>
  </div>
  <div style="margin-right:22px; margin-top:14px; clear:left;">
   <div style="height:2px;display:block;*display:none;width:auto;">
    &nbsp;
   </div>
   <div class="js_addr_div" id="addrsDiv">
    <table style="width:auto;display:none;margin-bottom:5px;" id="trSC" border="0" class="i" cellspacing="0" cellpadding="0">
     <tbody>
      <tr>
       <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
        <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
         <a href="javascript:;" title="点击选择要分别发送的联系人" id="sc_btn">分别发送</a>
        </div></td>
       <td style="*padding-right:4px;" width="99%" class="content_title">
        <div class="noime div_txt" id="scAreaCtrl">
         &nbsp;
        </div><textarea readonly="readonly" tabindex="2" disabled="" onfocus="setFocus('sc');" class="txt input_wd rev noime" size="100" name="sc" id="sc">乔布简历会员齐分享</textarea></td>
      </tr>
     </tbody>
    </table>
    <table id="trTO" style="width:auto;margin-bottom:5px;" class="i" border="0" cellspacing="0" cellpadding="0">
     <tbody>
      <tr>
       <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
        <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
         <a href="javascript:;" title="点击选择收件人" id="to_btn">收件人</a>
        </div></td>
       <td style="*padding-right:4px;" width="99%" class="content_title">
        <div class="noime div_txt" id="toAreaCtrl">
         &nbsp;
        </div><textarea readonly="readonly" accesskey="t" tabindex="1" onfocus="setFocus('cc');" class="noime txt input_wd rev" name="to" id="to" title="您可以在右侧中选择联系人给他们发信">乔布简历会员齐分享</textarea><input type="hidden" value="" name="swap3" id="swap3"></td>
      </tr>
     </tbody>
    </table>
    <table style="width:auto;display:none;margin-bottom:5px;" id="trCC" class="i" border="0" cellspacing="0" cellpadding="0">
     <tbody>
      <tr>
       <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
        <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
         <span clss="f_family">&nbsp;&nbsp;&nbsp;</span>
         <a href="javascript:;" title="点击选择要抄送的联系人" id="cc_btn">抄送</a>
        </div></td>
       <td style="*padding-right:4px;" width="99%" class="content_title">
        <div class="noime div_txt" id="ccAreaCtrl">
         &nbsp;
        </div><textarea readonly="readonly" tabindex="2" disabled="" onfocus="setFocus('cc');" class="txt input_wd rev noime" size="100" name="cc" id="cc">乔布简历会员齐分享</textarea></td>
      </tr>
     </tbody>
    </table>
    <table style="width:auto;display:none;margin-bottom:5px;" id="trBCC" class="i" border="0" cellspacing="0" cellpadding="0">
     <tbody>
      <tr>
       <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
        <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
         <span clss="f_family">&nbsp;&nbsp;&nbsp;</span>
         <a href="javascript:;" title="点击选择要密送的联系人" id="bcc_btn">密送</a>
        </div></td>
       <td style="*padding-right:4px;" width="99%" class="content_title">
        <div class="noime div_txt" id="bccAreaCtrl">
         &nbsp;
        </div><textarea readonly="readonly" tabindex="2" disabled="" onfocus="setFocus('bcc');" class="txt input_wd rev noime" size="100" name="bcc" id="bcc">乔布简历会员齐分享</textarea></td>
      </tr>
     </tbody>
    </table>
    <div style="overflow:hidden; min-height:22px; _zoom:1;" id="addrOper">
     <div class="input_title addrtitle">
      <div style="float:left;white-space:nowrap;padding-left:1px;">
       <span><a href="javascript:;" title="什么是抄送： 同时将这一封邮件发送给其他联系人。" id="aCC">添加抄送</a>&nbsp;-&nbsp;<a href="javascript:;" title="什么是密送： 同时将这一封邮件发送给其他联系人，但收件人及抄送人不会看到密送人。" id="aBCC">添加密送</a>&nbsp;|&nbsp;</span>
       <span style="display:none;">每个收件人将收到单独发给他/她的邮件。</span>
       <a href="javascript:;" title="什么是分送： 会对多个人一对一发送。每个人将收到单独发给他/她的邮件。" id="aSC">分别发送</a>
      </div>
      <div style="display:none;" class="addrtitle right">
       您是否还要找：
       <span id="addrAssociation"><a href="">nicoyang</a>，<a href="">angusdu</a>, <a href="">allen</a></span>
      </div>
      <div class="clr"></div>
     </div>
    </div>
   </div>
   <table style="display:none;width:auto;margin-bottom:5px;" class="i" border="0" cellspacing="0" cellpadding="0" id="addrUrlCreator">
    <tbody>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;"></td>
      <td id="receiverMsgContainer" width="99%"></td>
     </tr>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
       <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
        收件人
       </div></td>
      <td style="*padding-right:4px;" width="99%" class="content_title">
       <div class="attbg bd urlcreator_to">
        <span class="ico_urlcreator"></span>
        <span>网页生成助手</span>
        <a title="移除" class="ico_close_mini" onclick="rmHelper('urlcreator');"></a>
       </div>
       <div class="addrtitle" style="margin:0 0 8px 0;clear:both;">
        试试给助手发封邮件。它会把邮件转成网页，并把网页的链接地址回复给你。
        <a target="_blank" href="http://service.mail.qq.com/cgi-bin/help?subtype=1&amp;&amp;no=1001014&amp;&amp;id=23">了解更多</a>
       </div></td>
     </tr>
    </tbody>
   </table>
   <table style="display:none;width:auto;margin-bottom:5px;" class="i" border="0" cellspacing="0" cellpadding="0" id="addrQzone">
    <tbody>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;"></td>
      <td id="receiverMsgContainer" width="99%"></td>
     </tr>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
       <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
        收件人
       </div></td>
      <td style="*padding-right:4px;" width="99%" class="content_title">
       <div class="attbg bd urlcreator_to">
        <span class="ico_addrqzone"></span>
        <span>发表到我的Qzone</span>
        <a title="移除" class="ico_close_mini" onclick="rmHelper('qzone');"></a>
       </div>
       <div class="addrtitle" style="margin:0 0 8px 0;clear:both;">
        给“发表到我的Qzone”发信，即可把邮件内容以日志形式发表到Qzone上。
        <a target="_blank" href="http://service.mail.qq.com/cgi-bin/help?subtype=1&amp;&amp;no=242&amp;&amp;id=23">了解更多</a>
       </div></td>
     </tr>
    </tbody>
   </table>
   <table style="width:auto;margin:5px 0;" class="i" border="0" cellspacing="0" cellpadding="0">
    <tbody>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;"></td>
      <td id="receiverMsgContainer" width="99%"></td>
     </tr>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
       <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
        <span title="主题是一封邮件的标题。">主题</span>
       </div></td>
      <td style="*padding-right:4px;" width="99%" class="content_title">
       <div unselectable="on" style="cursor: text;" class="div_txt">
        <a style="display:block;float:right;width:24px;text-decoration:none;" hidefocus=""><input class="pointer" hidefocus="" id="cpanelBtn" title="在QQ邮箱之间发送邮件时使用彩色标题。" type="button"></a>
        <div style="margin-right:32px;height:16px;">
         <div style="_position:absolute;_width:100%;">
          <input onfocus="showSubjectMsg(false);" value="" style="word-break:break-all;height:16px;line-height:16px;width:99%;border-width:0;" class="" tabindex="2" autocomplete="off" type="text" name="subject" title="主题是一封邮件的标题，可不填。" id="subject">
         </div>
        </div>
       </div></td>
     </tr>
    </tbody>
   </table>
  </div>
  <div onmousedown="QMAttach.hideDragAndDropContainer();" style="padding-top:10px;padding-bottom:8px;" class="input_title"> 
   <span id="attachupload" style="display:none"></span>
   <span id="composecontainer"><span class="compose_toolbtn qmEditorAttach dragAndDropTrap_box"><span onmouseout="getTop().rmClass(getTop().finds('a',this)[0],'underline');" onmouseover="getTop().addClass(getTop().finds('a',this)[0],'underline');" id="AttachFrame" title="添加小于 50M 的文件作为附件" sizelimit="50"><a hidefocus="" onmousedown="return false;" onclick="return false;" class="compose_toolbtn_text ico_att"><span id="sAddAtt1">添加附件</span><span id="sAddAtt2" style="display:none;" onmousedown="return false;" onclick="return false;">继续添加</span></a></span><a id="moreupload" class="ico_moreupload"></a></span><span id="bigAttachLink" title="可以向任何邮箱发送最大 3G 的附件" onclick="initFileTransporter();return false" ftnv2="true" onmousedown="return false;" class="compose_toolbtn qmEditorAttachBig"><a hidefocus="" class="compose_toolbtn_text ico_attbig">超大附件</a></span></span>
   <span id="QMEditorToolBarPlusArea"></span>
  </div> 
  <div class="attbg" id="AttList" style="margin:0 211px 0 69px; _margin-right:231px;">
   <div id="editor_bgmusic_container" style="display:none;padding:7px;"></div>
   <div id="attachContainer" style="display:none;">
    <div id="exist_file"></div>
    <div id="filecell"></div>
    <div id="BigAttach"></div>
   </div>
  </div>
  <div style="display:none; line-height: 25px; padding: 0 0 0 10px; margin: 0 210px 5px 68px;" id="encrypt_mail_tips" class="input_title infobar">
   这是一封加密邮件，对方需要解密才能查看邮件内容。
  </div>
  <div style="margin-right:20px;margin-top:-5px;">
   <table style="width:auto;" border="0" cellspacing="0" class="i" cellpadding="0">
    <tbody>
     <tr>
      <td nowrap="" valign="top" style="*width:58px;*padding-right:10px;">
       <div nowrap="" style="width:58px;padding-right:10px;text-align:right;padding-top:6px">
        正文
       </div></td>
      <td valign="top" style="padding-top:1px!important;padding-top:0;padding-right:0;width:99%;" class="content_title"><textarea readonly="readonly" name="content__html" id="content">乔布简历会员齐分享</textarea>
       <div acckey="q" tindex="3" style="height:304px;" id="QMEditorArea">
        <script>try{document.write( outputDataLoading( true ) );}catch(e){}</script>
       </div></td>
     </tr>
    </tbody>
   </table>
  </div>
  <div class="settingtable" style="margin-top:5px;height:29px;float:left;width:100%;" id="composeMoreOpt">
   <div style="margin:8px 0 0 68px">
    <div style="float:left; height:20px; display:none;">
     <div style="float:left;" id="Senderdiv"></div>
     <span class="left addrtitle">&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;</span>
     <input name="sendmailname" id="sendmailname" type="hidden">
    </div>
    <div id="signSelContainer" class="left"></div>
    <div class="left">
     <span id="otherComposeOptionBtn" style="cursor:pointer;">其他选项<img class="arrowdown" src="http://rescdn.qqmail.com/zh_CN/htmledition/images/spacer104474.gif"></span>&nbsp;
    </div>
    <div class="clr"></div>
   </div>
   <div style="display:none;" id="otherComposeOptionCntr">
    <div style="padding:6px 8px;background:#FFF;margin:0 210px 8px 68px;margin-right:213px\9;padding-top:3px\9;_padding-top:10px;line-height:22px;overflow:hidden;zoom:1;" class="qqshowbd">
     <span style="margin-right:6px;" class="nowrap left"><input style="margin:2px 1px 0 0;_margin:-5px 1px 0 0;vertical-align:middle;" value="1" name="savesendbox" tabindex="8" id="savesendbox" type="checkbox" title="勾选保存，邮件发送成功后，可返回已发送文件夹 中查看此封已发邮件。"><label title="勾选保存，邮件发送成功后，可返回已发送文件夹 中查看此封已发邮件。" for="savesendbox" style="cursor:pointer;vertical-align:middle;">保存到"已发送"</label>&nbsp;&nbsp;<span style="display:none" id="auto_save_span"><input style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" value="1" name="auto_save" id="auto_save" type="checkbox" title="勾选保存，邮件发送成功后，可返回已发送文件夹 中查看此封已发邮件。"><label for="auto_save" style="cursor:pointer;vertical-align:middle;">每次发信自动保存到"已发送"</label>&nbsp;&nbsp;</span><input style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" value="urgency" title="邮件投递优先级" name="priority" tabindex="9" id="high" type="checkbox"><label title="邮件投递优先级" for="high" style="cursor:pointer;vertical-align:middle;">紧急</label>&nbsp;&nbsp;<input style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" tabindex="10" value="receipt" name="receipt" id="receipt" type="checkbox" title="启用回执功能 您可以了解收件人是否阅读了您发送的邮件"><label title="启用回执功能 您可以了解收件人是否阅读了您发送的邮件" for="receipt" style="cursor:pointer;vertical-align:middle;">需要回执</label></span>
     <wbr>
     <span style="margin-right:6px;" class="nowrap left"><input title="把邮件内容切换成纯文本 它将无法插入图片、表情，以及 将丢失正文颜色等" onclick="changeContentType(this);" type="checkbox" style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" value="text" name="contenttype" id="contenttype"><label title="把邮件内容切换成纯文本 它将无法插入图片、表情，以及 将丢失正文颜色等" for="contenttype" style="cursor:pointer;vertical-align:middle;">纯文本</label>&nbsp;&nbsp;<input title="" onclick="" style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" type="checkbox" value="1" name="noletter" id="noletter"><label title="" for="noletter" style="cursor:pointer;vertical-align:middle;">使用信纸</label></span>
     <wbr>
     <span style="position:relative;" class="nowrap left"><input style="margin:2px 1px 0 0;_margin-top:-5px;vertical-align:middle;" value="" id="secmailcode" name="secmailcode" type="checkbox" title="加密"><label for="secmailcode" style="cursor:pointer;vertical-align:middle;">对邮件加密</label></span>
    </div>
    <div class="clr"></div>
   </div>
  </div>
  <div class="clr"></div>
 </div>
	"""
		val aa = Jsoup.parse(bb)
		val cc = aa.select("div#sendtimepadding").empty.first
		val nodes = {
			<span>收件人:</span><input type="text" name="to" /> 
			<span>主题:</span><input style="word-break:break-all;height:16px;line-height:16px;width:99%;border-width:0;" type="text" name="subject">积分分享计划</input> 
			<span>正文:</span><textarea name="content__html" id="content"><div><b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' /></div></textarea> 
		}
		cc.append(nodes.mkString)
		
		
		
		/*cc.appendElement("input").attr("type", "text").attr("name", "to")
		cc.appendElement("input").attr("type", "text").attr("name", "subject")
		.attr("style", "word-break:break-all;height:16px;line-height:16px;width:99%;border-width:0;")
		.html("<div><b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' /></div>")
		cc.appendElement("input").attr("type", "textarea").html("<div><b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' /></div>").attr("readonly", "readonly").attr("name", "content__html").attr("id", "content")
		cc.appendElement("input").attr("type", "submit").attr("value", "发送")*/
	println(cc.outerHtml)
}