(function (A, aN) {
    var ku = {},
        AG = {};

    function dFJ(Dx, bPQ, eV) {
        if (!Dx) {
            return;
        }
        function showTips(aq) {
            A.show(A.S("capTip"), true);
        }

        function hideTips() {
            A.show(A.S("capTip"), false);
        }

        var JZ = -1;
        A.addEvents(Dx, {
            keydown: function (aq) {
                var hk = aq.keyCode || aq.charCode
                if (hk == 20) {
                    if (JZ == 0) {
                        showTips(aq);
                        JZ = 1;
                    }
                    else if (JZ == 1) {
                        hideTips();
                        JZ = 0;
                    }

                }
            },
            keypress: function (aq) {
                var hk = aq.keyCode || aq.charCode,
                    EA = aq.shiftKey;
                if ((hk >= 65 && hk <= 90 && !EA)
                    || (hk >= 97 && hk <= 122 && EA)) {
                    JZ = 1
                    showTips(aq);
                }
                else if ((hk >= 97 && hk <= 122 && !EA) || (hk >= 65 && hk <= 90 && EA)) {
                    JZ = 0;
                    hideTips();
                }
                else {
                    hideTips();
                }
            },
            blur: function () {
                hideTips();
            }
        });
    };
    function dCG(bbR) {
        var ir = typeof bbR == "string" ? [bbR] : bbR;

        function fQ() {
            if (ir.length) {
                for (var i = 0; i < ir.length; i++) {
                    dwW(ir[i]);
                }
            }
        }


        function dwW(aP) {
            var xB = "label_",
                gA = A.S(xB + aP),
                ce = A.S(aP);
            if (!gA || !ce)return;

            A.addEvent(ce, "keyup", zd);
            A.addEvent(ce, "keydown", zd);
            A.addEvent(ce, "input", zd);
            A.addEvent(ce, "click", zd);


            ce.onfocus = function () {
                this.setAttribute("_focus", 1);
                if (aP == "p") {
                    aAT();
                }
                zd(this);
                yC(true)
            };
            ce.onblur = function () {
                this.removeAttribute("_focus");
                zd(this);
                yC()
            };

            function yC(auo) {
                gA.style.color = auo ? "#ccc" : "";

            };
            function zd() {
                gA.innerHTML = ce.value ? "&nbsp;" : gA.getAttribute("default_txt");
                gA.style.color = ce.value ? "" : "#ccc";
            };
            function bAS(o, l) {
                if (o.value) {
                    l.innerHTML = "&nbsp;";
                }
                else {
                    l.innerHTML = l.getAttribute("default_txt");
                }
            };
            bAS(ce, gA);


            setTimeout(function (o, l) {
                return function () {
                    bAS(o, l);
                }
            }(ce, gA), 100);
            setInterval(function (o, l) {
                return function () {
                    bAS(o, l);
                }
            }(ce, gA), 800);
        };

        fQ();
    };
    function djQ(ceP, wI, EB, ckN, gF) {
        var ao = this;

        function fQ() {
            ao.aiD = dgy(ceP);
            ao.dGn = ceP;
            ao.bfL = wI;
            ao.IS = EB;
            ao.dKo = ckN;


            for (var i = 0; i < EB.length; i++) {
                if (EB[i] == wI.value) {
                    wI.style.fontSize = ckN[i];
                    break;
                }
            }

            A.addEvent(wI, "click", zd);
        };
        function cfO(dvy) {

            var aVq = 0;
            var bv = ao.IS;
            for (var i = bv.length; i > 0; i--) {
                if (bv[i - 1].indexOf(dvy) == 0) {
                    aVq = i - 1
                }
            }
            return aVq;
        };
        function zd(cL) {
            var qP = ao.dGn.value,

                bBh = ao.bfL.value,
                lN = cL || event,
                bHS = lN.keyCode == 38 || lN.keyCode == 40;

            if (lN.type == "click" || bHS) {
                var oz = [],
                    bv = ao.IS,
                    aVq = cfO(bBh);

                for (var i = 0; i < bv.length; i++) {
                    oz.push([
                        '<div class="auto_item ', aVq == i ? 'selected auto_select' : '', '" ',
                        ' alias="', qP, '" domain="', EB[i], '" title="\u4F7F\u7528 ', qP, EB[i], ' \u767B\u5F55" ',
                        ' fontsize="', ao.dKo[i], '"',
                        '>',
                        '<span class="ico_selected"></span>', EB[i],
                        '</div>'
                    ].join(""));
                }

                {
                    ao.aiD.innerHTML = oz.join("");
                }


                var bv = ao.aiD.childNodes;

                for (var i = 0, l = bv.length; i < l; i++) {
                    (function (aw, ea) {
                        A.addEvent(aw, "mousedown", function () {
                            afL();
                            aAT();
                        });
                        A.addEvent(aw, "mouseover", function () {
                            azn(ea);
                        });
                    })(bv[i], i);
                }

                if (lN.type == "keydown") {

                    if (lN.keyCode == 38) {
                        if (typeof ao.Yw == "undefined") {
                            ao.Yw = bv.length - 1;
                        }
                        else {
                            ao.Yw--;
                            if (ao.Yw < 0) {
                                ao.Yw = bv.length - 1;
                            }
                        }

                        afL();
                        A.preventDefault(lN);
                        return false;
                    }

                    else if (lN.keyCode == 40) {
                        if (typeof ao.Yw == "undefined") {
                            ao.Yw = 1;
                        }
                        else {
                            ao.Yw++;
                            if (ao.Yw > bv.length - 1) {
                                ao.Yw = 0
                            }
                        }

                        afL();
                        A.preventDefault(lN);
                        return false;
                    }

                    else if (lN.keyCode == 13) {


                        A.preventDefault(lN);
                    }

                    else if (lN.keyCode == 9) {


                    }
                }
                else {
                    ao.show();
                }
            }


        };
        function azn(oF) {

            var bv = ao.aiD.childNodes;
            for (var i = 0, l = bv.length; i < l; i++) {
                if (oF == i) {
                    A.addClass(bv[i], "auto_select");
                    ao.Yw = i;
                }
                else {
                    A.rmClass(bv[i], "auto_select");
                }
            }
        };
        function afL() {

            var bv = ao.aiD.childNodes,
                bFi = bv[ao.Yw],
                hu = ao.bfL.value,
                aVq = cfO(hu),
                bBh = "|" + hu,
                dqU = ["|", ao.IS.join("|"), "|"].join("");

            if (bFi) {

                if (hu && (dqU.indexOf(bBh) > -1)) {
                    ao.bfL.value = bFi.getAttribute("domain");
                    ao.bfL.style.fontSize = bFi.getAttribute("fontsize");
                }
                ao.hide();
                gF && setTimeout(function () {
                    gF();
                });
            }
        };
        function dgy(deO) {
            var Ti = document.createElement("div");
            Ti.className = "autocomplete";
            Ti.id = "auto_container";

            Ti.setAttribute("tabindex", "-1");
            Ti.setAttribute("hidefocus", "true");
            Ti.onblur = function () {
                ao.hide();
            };
            A.show(Ti, false);
            deO.parentNode.appendChild(Ti);
            return Ti;
        };

        ao.show = function () {
            A.show(ao.aiD, true);
            A.setFocus(ao.aiD);
            A.addClass(ao.aiD, "login_domains_show");
        };
        ao.hide = function () {
            A.show(ao.aiD, false);
            A.rmClass(ao.aiD, "login_domains_show");
        };

        fQ();
    };
    function cRN(HQ) {
        var IR = A.S('dialog_wrap'),
            azA = A.S('mask');

        if (HQ) {
            A.hasClass(IR, "dropmenu")
            && A.rmClass(IR, "dropmenu");
            !A.hasClass(IR, "popup")
            && A.addClass(IR, "popup");
            A.addClass(azA, "mask-open");
            A.show(IR, true);
            auy();
        }
        else {
            A.show(IR, false);
            A.rmClass(azA, "mask-open");
        }
    };
    function dVz(HQ, ahH) {
        var IR = A.S('dialog_wrap'),
            aUX = A.S('login_menu'),
            azA = A.S('mask');

        A.rmClass(azA, "mask-open");
        !A.hasClass(ahH, "drop")
        && A.addClass(ahH, "drop");

        if (HQ) {
            A.hasClass(IR, "popup")
            && A.rmClass(IR, "popup");
            !A.hasClass(IR, "dropmenu")
            && A.addClass(IR, "dropmenu");

            A.show(IR, true);
        }
        else {
            A.show(aUX, true);
        }

        if (!document.onmousedown) {
            document.onmousedown = function (aq) {
                aq = aq || event;
                var bjr = aq.target || aq.srcElement;
                if (A.contains(ahH, bjr)) {
                    return;
                }
                if (!A.contains(IR, bjr) && A.hasClass(IR, "dropmenu")) {
                    A.show(IR, false);
                    A.rmClass(IR, "dropmenu");
                    A.hasClass(ahH, "drop")
                    && A.rmClass(ahH, "drop");
                }
                else if (aUX && aUX.style.display != "none" && !A.contains(aUX, bjr)) {
                    A.show(aUX, false);
                    A.hasClass(ahH, "drop")
                    && A.rmClass(ahH, "drop");
                }
            };
        }
        auy();
    };
    function djZ(dsr, dsT) {
        var ao = this;
        ao.dHu = 0;
        ao.aca = A.getElementsByClassName('slide-ctrl', A.S(dsr), 'a');
        ao.chM = A.getElementsByClassName('slide-content', A.S(dsT), 'div');

        function dmC(ea) {
            ao.dHu = ea;
            ku.sSubType = ea;
            for (var i = 0; i < ao.aca.length; i++) {
                if (A.hasClass(ao.aca[i], "current")) {
                    A.rmClass(ao.aca[i], "current");
                    A.rmClass(ao.chM[i], "current");
                }
            }
            if (!A.hasClass(ao.aca[ea], "current")) {
                A.addClass(ao.aca[ea], "current");
                A.addClass(ao.chM[ea], "current");
            }
        };
        function fQ() {
            for (var i = 0; i < ao.aca.length; i++) {
                (function (ea) {
                    ao.aca[ea].onclick = function () {
                        dmC(ea);
                    }
                })(i);
            }
        };
        fQ();
    };


    function azz(dme, dmf) {
        var ei = document.createElement("img"),

            vY = (AG.ossDomain == "" ? "https://rl.mail.qq.com" : AG.ossDomain) + "/cgi-bin/getinvestigate?stat=newlogin&log1=#log1#&log2=#log2#&r=#random#";
        ei.src = vY.replace("#log1#", dme).replace("#log2#", dmf).replace("#random#", Math.random());
        A.show(ei, false);
        document.body.appendChild(ei);
        ei = null;
    }

    function cpF(eKw, ix) {
        var ei = document.createElement("img"),

            vY = [(AG.ossDomain == "" ? "https://rl.mail.qq.com" : AG.ossDomain), "/cgi-bin/getinvestigate?stat=loginerr&code=", eKw, "&err=", ix, "&r=", Math.random()].join("");
        ei.src = vY;
        A.show(ei, false);
        document.body.appendChild(ei);
        ei = null;
    }

    function fNX() {
        var ei = document.createElement("img"),

            vY = [(AG.ossDomain == "" ? "https://rl.mail.qq.com" : AG.ossDomain), "/cgi-bin/getinvestigate?kvclick=getinvestigate|login|verifycode|imgerror", "&r=", Math.random()].join("");
        ei.src = vY;
        A.show(ei, false);
        document.body.appendChild(ei);
        ei = null;
    }

    function kx(Fq, aXo) {
        ku.bHB = {
            "pt": 10,
            "qm": 20,
            "op": 30,
            "dm": 40,
            "d2": 50
        }[AG.sLoginType];
        ku.HC = Fq || 0;
        if (Fq == 1) {

            ku.bzN = 1;
        }

        if (Fq == 4 && AG.bUsingPT) {
            aXo == ku.cpf && ku.aPb++;
            aXo && (ku.bLZ = aXo);

            azz([ku.bHB + ku.HC, ku.GF, ku.aEq].join(","), [ku.bzN, ku.bLZ, ku.aPb, ku.bBT].join(","));
        }

        else if (Fq == 5 && AG.bUsingPT) {
            azz([ku.bHB + ku.HC, ku.GF, ku.aEq].join(","), [ku.bzN, "", ku.aPb, ku.bBT].join(","));
        }


    };

    function dwe(gF) {
        if (AG.bUsingPT) {


            setTimeout(function () {
                gF && gF();
            }, 500);
            setInterval(function () {
                gF && gF();
            }, 3 * 60 * 1000);
        }
    };

    function mC(ecP) {
        var dJU = A.S('uin'),
            cuP = A.S('domain'),
            qx = A.S('u'),

            ckP = A.S('u1'),
            bgO = "",
            qP, hu;

        if (cuP) {
            qP = dJU.value;
            hu = cuP.value;
            qx.value = qP ? (qP + hu) : "";
        }
        else if (AG.bUsingPT) {
            var cof = qx.value;
            qP = cof.split("@")[0];
            hu = cof.split("@")[1];
            hu = hu ? ("@" + hu) : "";
            bgO = ku.sSubType;
        }

        if (AG.bUsingPT) {

            ku.GF = qP;
            ku.cJE = hu ? hu : (/\d+/.test(qP) ? "qq.com" : "");

            if (hu == "@vip.qq.com") {
                ku.aEq = "vip";
            }
            else if (hu == "@foxmail.com") {
                ku.aEq = "fox";
            }
            else if (hu == "@qq.com") {
                ku.aEq = "";
            }
            else {
                ku.aEq = hu || "__noInput__";
            }


            ckP.value = A.urlReplacer(A.extend(

                {
                    "ss": (A.S('remerber_password') && A.S('remerber_password').checked) ? "1" : ""
                },

                ecP ? {
                    "validcnt": ku.aPb,
                    "clientaddr": qx.value
                } : {},

                (function (aD) {
                    switch (aD) {
                        case"op":
                            return({
                                "sub": bgO
                            });
                        case"dm":
                            return({



                                "errtemplate": "dm_loginpage",
                                "aliastype": "other",
                                "dmtype": "domain",
                                "delegate_url": encodeURIComponent(A.S("delegate_url").value),
                                "s": A.S("s").value,
                                "loginEntry": A.S("loginEntry").value,
                                "target": A.S("target").value,
                                "name": encodeURIComponent(A.S("name").value),
                                "bcid": A.S("bcid").value,
                                "token": A.S("token").value
                            });
                        default:
                            return;
                    }
                })(AG.sLoginType)
            ), ckP.value);
        }
        return true;
    };

    function bsB(aD) {
        kx(2);

        if (AG.bUsingPT) {

            mC(true);
            if (aD == "addr") {
                awk();
                return false;
            }


            var bwJ = A.S("p");
            if (bwJ.value.length > ku.cgz) {
                ku.bBT = 1;
                bwJ.value = bwJ.value.substr(0, ku.cgz);
            }

            try {
                var aCY = function () {
                    if (!ptui_checkValidate()) {

                        auy();
                    }
                };


                if (!pt.isLoadVC && !gCz()) {
                    pt.g_uin = "";
                    aAT();
                    setTimeout(function () {
                        aCY();
                    }, 1000);
                }
                else {
                    aCY();
                }

            }
            catch (e) {

                window['bIsAutoLogin'] = 1;
                aKw(false);
            }
            if (!ku.GF) {
                A.setFocus("uin");
            }
            kx(3);
            return false;
        }
        else {
            if (A.S("pp")) {
                var bhR = window.org_pass = A.S("pp").value,
                    fu = document.loginform;
                if (!bhR) {
                    awk("\u8BF7\u8F93\u5165\u72EC\u7ACB\u5BC6\u7801");
                    A.setFocus("pp");
                    kx(3);
                    return false;
                }


                if (AG.bNeedEncrypt) {
                    if (bhR.length > 170) {
                        fu.p.value = bhR;
                    }
                    else {
                        var PublicKey = "CF87D7B4C864F4842F1D337491A48FFF54B73A17300E8E42FA365420393AC0346AE55D8AFAD975DFA175FAF0106CBA81AF1DDE4ACEC284DAC6ED9A0D8FEB1CC070733C58213EFFED46529C54CEA06D774E3CC7E073346AEBD6C66FC973F299EB74738E400B22B1E7CDC54E71AED059D228DFEB5B29C530FF341502AE56DDCFE9",
                            PublicTs = fu.ts.value,
                            RSA = new RSAKey();
                        RSA.setPublic(PublicKey, "10001");

                        var Res = RSA.encrypt(fu.pp.value + '\n' + PublicTs + '\n');
                        if (Res) {
                            fu.p.value = hex2b64(Res);
                        }
                    }
                }
                else {
                    fu.p.value = bhR;
                }
            }

            if (A.S("verifyinput").style.display != "none" && !A.S("verifycode").value) {
                awk("\u8BF7\u8F93\u5165\u9A8C\u8BC1\u7801");
                A.setFocus("verifycode");
                kx(3);
                return false;
            }


            if (A.S("pp")) {
                fu.pp.value = fu.p.value;
            }

            kx(4);
        }
        return true;
    };
    function aKw(alT) {
        var kZ = A.S('btlogin');
        if (0 && alT) {
            kZ.value = (kZ.getAttribute("progress") || "\u767B\u5F55\u4E2D");
            kZ.parentNode.className = "login_btn_wrapper_disabled";
            kZ.setAttribute("loading", 1)
        }
        else {
            kZ.removeAttribute("loading");
            kZ.value = (kZ.getAttribute("default") || "\u767B\u5F55");
            kZ.parentNode.className = "login_btn_wrapper";
        }
    };
    function dEu() {
        if (AG.bUsingPT) {
            imgLoadReport();
        }
        kx(1);
    };
    function bOv() {
        if (AG.bUsingPT) {
            try {
                ptui_changeImg();
            }
            catch (e) {
                awk("errorPT", "img:" + e.message);
            }
        }
        else {
            A.S('vfcode').src = AG.sServerName + "/cgi-bin/getverifyimage?aid=23000101&f=html&ck=1&r=" + Math.random();
            A.setFocus("verifycode");
        }
    };
    function dyV(gH) {

        if (gH == ku.daV) {
            kx(5);
        }
        else {
            kx(4, gH);
        }

        cpF(gH);
        aKw(false);
    };
    function awk(cX, ix) {
        var exy = cX;
        aKw(false);
        if (!cX) {
            A.S("msgContainer").style.display = "none";
            return;
        }

        var bDI =
            {
                errorPT: "\u7F51\u7EDC\u5F02\u5E38\uFF0C\u90E8\u5206\u8D44\u6E90\u672A\u80FD\u62C9\u53D6\uFF0C\u8BF7\u5237\u65B0\u540E\u91CD\u8BD5\u3002",
                errorCheck: "\u7F51\u7EDC\u5F02\u5E38\uFF0C\u8BF7\u5237\u65B0\u540E\u91CD\u8BD5\u3002",
                errorSecondPwdNeedQQErr: "\u4E3A\u589E\u5F3A\u90AE\u7BB1\u5B89\u5168\u6027\uFF0C\u767B\u5F55\u65F6\u9700\u8981\u9A8C\u8BC1\u60A8\u7684QQ\u5BC6\u7801\u3002"
            },
            abC = A.S("msgContainer");

        bDI[cX] && (cX = bDI[cX]);

        if (dlJ()) {
            cX = bDI["errorSecondPwdNeedQQErr"]
        }
        abC.innerHTML = cX.replace(/[\,\.\!\?\uFF0C\u3002\uFF01]$/g, "");
        abC.style.display = "";


        if (AG.bUsingPT) {
            var eVb = 511,
                bXk = {
                    errorPT: 551,
                    errorCheck: 552,
                    errorSecondPwdNeedQQErr: 553
                }[exy] || -1;
            if (window.pt && pt.lang) {
                var n = -1;
                for (var i in pt.lang) {
                    n++;
                    if (cX.indexOf(pt.lang[i]) == 0) {
                        bXk = eVb + n;
                        break;
                    }
                }
            }

            bXk > -1
            && cpF(bXk, ix);
        }
    };
    function dlJ() {


        if (ku.bQm == AG.sLoginType
            && ku.aPb >= ku.cWG
            && ku.bLZ == ku.cpf
            && ku.HC == 4) {
            ku.dtm = 1;
            return true;
        }
        return false;
    };
    function aAT() {
        if (AG.bUsingPT) {
            mC();
            try {
                window.check && check();
            }
            catch (e) {


            }
        }
    };
    function auy() {
        var FR = ku.bQm == AG.sLoginType ? "uin" : "u",
            dts = [FR, "pp", "p", "verifycode"];
        for (var i = 0, l = dts.length; i < l; i++) {
            var aX = dts[i];
            if (A.S(aX) && !A.S(aX).value) {
                A.setFocus(aX);
                break;
            }
            aX = null;
        }
    };
    function bqx() {
        awk();
        setTimeout(function () {
            aAT();
            auy();
        });
    };
    function cEr(gF, daC) {
        var ga = AG.sLocale == "en_US" ? "login_div_1033.js" : "login_div.js",
            bsT = daC || [
                [!AG.bNeedEncrypt ?
                    ('https://ui.ptlogin2.qq.com/js/' + ga) :
                    ('http://imgcache.qq.com/ptlogin/ac/v9/js/' + ga), "UTF-8"],
                [AG.oResCfg && AG.oResCfg.sPtUrl || "", "GBK"]
            ];

        if (bsT.length && bsT[0][0]) {
            var aM = bsT.shift(),
                fm = document.createElement("script");
            fm.type = "text/javascript";
            fm.charset = aM[1];
            fm.src = aM[0];

            cPa();

            window["PTLOGIN_TIMER"] = +new Date();
            A.waitFor(
                function () {
                    return window["pt"];
                },
                function (fGm) {
                    if (fGm) {
                        typeof(window.onload) == "function" && ku.eZX
                        && window.onload();

                        cPa(true);
                        gF(fGm);


                        !bsT.length && cpF(typeof window["PTLOGIN_RESCODE"] == "undefined" ? 1009 : (1000 + window["PTLOGIN_RESCODE"]));
                    }
                    else {
                        if (bsT.length) {
                            dgl();
                            cEr(gF, bsT);
                        }
                        else {
                            dgl(true);
                            cPa(true);
                        }
                    }
                }
            );

            document.body.appendChild(fm);
        }
        ;
        function dgl(edr) {
            var dcw = {
                "pt": 7,
                "dm": 9
            }[AG.sLoginType] || 11;
            edr && (dcw++);
            (new Image()).src = A.urlReplacer({
                "jsfailtime": dcw,
                "r": Math.random()
            }, AG.oResCfg.sReportUrl);
        };
    };


    function gbq() {
        if (window['bIsAutoLogin']) {
            aAT();
            A.waitFor(
                function () {
                    return pt.isLoadVC || A.S("verifycode").value;
                },
                function (ph) {
                    ph && bsB();
                }
            );


        }
        else if (window['bIsAnythigInput']) {
            aAT();
        }
    };

    function gCz() {

        if (!pt.f_v.value) {
            return false;
        }

        var xB = [ku.GF, ku.cJE].join(""),
            qi = pt.f_v.value,
            cTC = ku.eWe[qi];


        if (cTC.sUserName != xB) {
            return false;
        }


        return+new Date() - cTC.nTimeStamp < 60000;
    };


    function fOv() {
        var fqz = function () {
            if (!window.pt) {
                return;
            }

            if (!pt.f_v.value) {
                return;
            }

            var xB = [ku.GF, ku.cJE].join(""),
                qi = pt.f_v.value,
                cTC = ku.eWe[qi];

            if (!cTC) {
                cTC = ku.eWe[qi] =
                {
                    nTimeStamp: +new Date(),
                    sUserName: xB
                };
            }

        };


        ku.fFZ
        && clearInterval(ku.fFZ);
        ku.fFZ = setInterval(fqz, 1000);

        fqz();
    };
    function cPa(dXi) {
        var hE = ["u", "p", "verifycode"];
        for (var i = 0; i < hE.length; i++) {
            (function () {
                if (!dXi) {

                    if (!A.S(hE[i]).tfocus) {
                        A.S(hE[i]).tfocus = A.S(hE[i]).focus;
                        A.S(hE[i]).focus = function () {
                        };
                    }
                }
                else {
                    if (i == 0) {

                        A.S(hE[i]).focus = function () {
                            try {
                                this.tfocus();
                            }
                            catch (e) {
                            }
                        }
                    }
                    else {
                        A.S(hE[i]).focus = A.S(hE[i]).tfocus;
                    }
                }
            })();
        }
        ;
    }

    ku = window["QMLogin"] = A.extend({




        changeImg: bOv,


        checkInput: bsB,


        judgeVC: aAT,


        switchMode: bqx,


        imgError: fNX,


        onLoadVC: dEu
    }, {
        init: function (ax) {
            AG = ax;


            A.extend(
                window,
                {

                    pt_init_end: function () {
                        gbq();
                    },

                    callback: function () {
                        try {
                            document.selection
                            && document.selection.empty()
                        }
                        catch (e) {
                        }

                        if (window["bIsAutoLogin"] || window["bIsAnythigInput"]) {


                            pt.switchPage();
                        }
                    },

                    ptlogin2_onResize: function () {
                        ax.onResize && ax.onResize.apply(this, arguments);
                    },

                    ptlogin2_onMibaoCancel: function () {
                        location.replace(location)
                    },

                    ptui_bos: function () {
                        dyV.apply(null, arguments);
                        ax.onSubmit && ax.onSubmit.apply(this, arguments);
                    },

                    pt_show_err: function () {
                        if (ku.bQm == AG.sLoginType && A.S("uin").getAttribute("_focus")) {
                            return;
                        }
                        awk.apply(null, arguments);
                    }
                }
            );


            if (ax.bNeedEncrypt && !ax.bUsingPT && ax.oResCfg.sEncryptUrl) {
                var fm = document.createElement("script");
                fm.type = "text/javascript";
                fm.charset = "GBK";
                fm.src = ax.oResCfg.sEncryptUrl;
                document.body.appendChild(fm);
                fm = null;
            }
        },


        ready: function () {
            if (AG.bUsingPT) {

                A.addEvent(window, "error", function (e) {

                    aKw(false);
                    cpF(919, e.message || "runtime script error!");
                });


                if (ku.eeu == AG.sLoginType) {
                    pt_need_qlogin = 0;
                }


                A.addEvent(document.loginform, "keydown", function (cL) {
                    window["bIsAnythigInput"] = true;
                });


                if (A.S("uin")) {
                    auy();
                    cEr(function (bj) {
                        if (A.S("u").value && !A.S("uin").value) {
                            A.S("uin").value = A.S("u").value.split("@")[0];
                            auy();
                        }
                    });
                }
                else {

                    var cEH = (A.getCookie("ptui_loginuin") || ""),
                        cYZ = (A.getCookie("qqmail_alias") || "");
                    if (!cYZ && cEH) {
                        A.setCookie("ptui_loginuin", cYZ);
                    }

                    cEr(function (bj) {

                        var dDM = A.S("u").getAttribute("default");
                        if (dDM) {
                            A.S("u").value = dDM;
                        }
                        auy();
                    });
                }
            }
            else {
                auy();
            }

            var ao = this,
                lc = {



                    "basic": function () {
                        fOv();

                        dwe(function () {
                                aAT();
                            }
                        );
                        A.S('btlogin').onclick = function () {
                            var kZ = this;
                            if (kZ.getAttribute("loading")) {


                                return false;
                            }
                            else {
                                aKw(true);
                            }
                        };
                    },


                    "caps": function () {
                        new dFJ(A.S("pp") || A.S("p"));
                    },


                    "label": function () {
                        new dCG(["uin", "u", "p", "pp", "verifycode"]);
                    },


                    "slide": function () {
                        new djZ('slide_ctrls', 'slide_contents');
                    },


                    "domain": function () {
                        var aGk = A.S("uin"),
                            aRR = A.S("domain");
                        if (aGk) {
                            new djQ(aGk, aRR, ku.dwx, ku.dwD);
                            A.addEvent(aGk, "blur", function () {
                                A.rmClass(aGk.parentNode, "active");
                            });
                            A.addEvent(aGk, "focus", function () {
                                A.addClass(aGk.parentNode, "active");
                            });

                            if (!-[1, ] && !window.XMLHttpRequest) {
                                A.addEvent(aRR, "mouseout", function () {
                                    A.rmClass(aRR.parentNode, "active");
                                });
                                A.addEvent(aRR, "mouseover", function () {
                                    A.addClass(aRR.parentNode, "active");
                                });
                            }
                        }
                    },


                    "popup": function () {
                        A.S("dialog_close").onclick = function () {
                            cRN(false);
                        };
                        A.S("login").onclick = function (cL) {
                            dVz(!AG.bIsLogin, this);
                        };

                        A.each(A.getElementsByClassName("button-blue"), function (aw) {
                                (function () {
                                    aw && aw.getAttribute("data-uigroup") == "dialog" && AG.sLoginType == ao.dWS
                                    && (aw.onclick = function (cL) {
                                        cL = cL || event;
                                        if (!AG.bIsLogin) {
                                            cRN(true);
                                            A.preventDefault(cL);
                                            return false;
                                        }
                                    });
                                })();
                            }
                        );
                    }
                },
                aFu = {

                    "pt": ["basic", "caps", "label", "domain"],

                    "op": ["basic", "caps", "label", "popup", "slide"],

                    "qm": ["basic", "caps", "label"],

                    "dm": ["basic"],

                    "d2": ["basic"]
                };


            A.each(AG.oUseCom || aFu[AG.sLoginType], function (dvc) {
                    lc[dvc].apply(ao);
                }
            );
        }
    }, {



        cWG: 2,
        cgz: 16,

        dwx: ["@vip.qq.com", "@qq.com", "@foxmail.com"],
        dwD: ["14px", "18px", "13px"],


        daV: "0",
        cpf: "3",


        ffR: "qm",
        bQm: "pt",
        dWS: "op",
        eeu: "dm",
        ffM: "d2",


        bHB: 0,
        HC: 0,


        aPb: 0,
        bzN: 0,
        dtm: 0,
        bBT: 0,
        aEq: "",
        cJE: "@qq.com",
        bLZ: "",
        eWe: {},
        GF: ""
    });

    A.addEvent(window, "load", function () {
            ku.eZX = !window["pt"];
        }
    );
})
    (function () {
        function hc(aP) {
            return document.getElementById(aP) || null;
        };
        function dIt(aS, ci, nE, dE, la, qJ) {
            if (aS) {
                var gr = [
                        '$name$=$value$; ',
                        !nE ? '' : 'expires=$expires$; ',
                        'path=$path$; ',
                        'domain=$domain$; ',
                        !qJ ? '' : '$secure$'
                    ].join(""),
                    dFc = {
                        name: aS,
                        value: encodeURIComponent(ci || ""),
                        expires: nE && nE.toGMTString(),
                        path: dE || '/',
                        domain: la || "qq.com",
                        secure: qJ ? "secure" : ""
                    };
                for (var i in dFc) {
                    gr = gr.replace("\$" + i + "\$", dFc[i]);
                }
                ;

                document.cookie = gr;
                return true;
            }
            else {
                return false;
            }
        };
        function efx(JE) {
            return JE.replace(/([\^\.\[\$\(\)\|\*\+\?\{\\])/ig, "\\$1");
        }

        function erW(aS) {
            return(new RegExp([
                "(?:; )?", efx(aS), "=([^;]*);?"
            ].join("")
            )).test(document.cookie) && decodeURIComponent(RegExp["$1"]);
        };
        function eyb(aS, dE, la) {
            dIt(aS, "", new Date(0), dE, la);
        }

        function AZ(aw, aD, mN, nR) {
            if (aw && mN) {
                if (aw.addEventListener) {
                    aw[nR ? "removeEventListener" : "addEventListener"](
                        aD, mN, false
                    );
                }
                else if (aw.attachEvent) {
                    aw[nR ? "detachEvent" : "attachEvent"]("on" + aD,
                        mN
                    );
                }
                else {
                    aw["on" + aD] = nR ? null : mN;
                }
            }
            return aw;
        };
        function dwf(str) {
            return str.replace(/(^\s+|\s+$)/g, "");
        };
        function aCO(aS, ci, amg) {
            var qe = new RegExp("([?&]" + aS + "=)([^&#]*)?"),
                cG = ci ? ci : "";

            return(qe.test(amg) ?
                amg.replace(qe, "$1" + cG)
                : [amg, "&", aS, "=", cG].join(""));
        };


        return({
            S: hc,
            trim: dwf,
            addEvent: AZ,
            setCookie: dIt,
            getCookie: erW,
            delCookie: eyb,
            addEvents: function (eo, kD, nR) {
                for (var aD in kD) {
                    AZ(eo, aD, kD[aD], nR);
                }
                return eo;
            },
            setFocus: function (nz) {
                var aB = typeof nz == "string" ? hc(nz) : nz;
                if (aB) {
                    try {
                        aB.focus();
                        aB.onfocus();
                    } catch (e) {
                    }
                    ;
                }
            },
            show: function (ub, mR, en) {
                var eA = (typeof(ub) == "string" ? hc(ub, en) : ub);
                if (eA) {
                    eA.style.display = (mR ? "" : "none");
                }
                return eA;
            },
            preventDefault: function (evt) {
                if (evt) {
                    evt.preventDefault ? evt.preventDefault() : (evt.returnValue = false);
                }
            },

            hasClass: function (aw, iI) {
                return(" " + aw.className + " ").indexOf(" " + iI + " ") > -1;
            },
            addClass: function (aw, iI) {
                var ji = " " + aw.className + " ";
                if (ji.indexOf(" " + iI + " ") < 0) {
                    aw.className += aw.className ? " " + iI : iI;
                }
            },
            rmClass: function (aw, iI) {
                if (iI) {
                    var ji = " " + aw.className + " ";
                    ji = ji.replace(" " + iI + " ", " ");
                    aw.className = dwf(ji);
                }
                else {
                    aw.className = "";
                }
            },
            extend: function () {
                for (var by = arguments, rz = by[0], i = 1, aK = by.length; i < aK; i++) {
                    var tY = by[i];
                    for (var j in tY) {
                        rz[j] = tY[j];
                    }
                }
                return rz;
            },
            waitFor: function (KC, Ci, uV, sC) {
                var gL = 0,
                    jN = uV || 500,
                    KJ = (sC || 10 * 500) / jN;

                function avZ(ph) {
                    try {
                        Ci(ph)
                    }
                    catch (bq) {

                    }
                };

                (function () {
                    try {
                        if (KC()) {
                            return avZ(true);
                        }
                    }
                    catch (bq) {

                    }

                    if (gL++ > KJ) {
                        return avZ(false);
                    }

                    setTimeout(arguments.callee, jN);
                })();
            },
            getElementsByClassName: function (aar, aw, lI) {
                if (document.getElementsByClassName) {
                    return document.getElementsByClassName(aar);
                }
                else {
                    aw = aw || document;
                    lI = lI || "*";
                    var iP = [],
                        amr = (lI == '*' && aw.all) ? aw.all : aw.getElementsByTagName(lI),
                        i = amr.length;
                    aar = aar.replace(/\-/g, '\\-');
                    var iB = new RegExp('(^|\\s)' + aar + '(\\s|$)');
                    while (--i >= 0) {
                        if (iB.test(amr[i].className)) {
                            iP.push(amr[i]);
                        }
                    }
                    return iP;
                }
            },
            each: function (ee, afFunc) {
                if (typeof ee.length == "number")
                    for (var i = 0, l = ee.length; i < l; i++) {
                        afFunc && afFunc.apply(null, [ee[i], i])
                    }
                else
                    for (var i in ee) {
                        afFunc && afFunc.apply(null, [ee[i], i])
                    }
                return ee;
            },
            contains: function (bkl, ciz) {
                if (bkl.contains) {
                    return bkl.contains(ciz);
                }
                else if (bkl.compareDocumentPosition) {
                    var eI = bkl.compareDocumentPosition(ciz);
                    return eI == 20 || eI == 0;
                }

                return false;
            },
            urlReplacer: function () {
                var bJ = arguments[0],
                    fE = (typeof bJ == "object" ? arguments[1] : arguments[2]) || location.href,
                    fE = fE + (fE.indexOf("?") > -1 ? "" : "?"),
                    aPC = fE.substr(0, fE.indexOf("?")),
                    aY = fE.substr(fE.indexOf("?"), fE.length);
                switch (typeof bJ) {
                    case"object":
                        for (var i in bJ) {
                            aY = aCO(i, bJ[i], aY);
                        }
                        break;
                    case"string":
                        aY = aCO(arguments[0], arguments[1], aY);
                        break;
                    default:
                }
                return aPC + aY;
            }
        });
    }());
