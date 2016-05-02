/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.scriptedflow.vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.scriptedflow.vm.pvo.ParamValueObject;
import de.schlund.pfixcore.workflow.ExtendedContext;
import de.schlund.pfixxml.PageAliasResolver;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.SPDocument;

/**
 * Executes scripts that have been compiled previously.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptVM {
    private Instruction[] il;

    private int ip = 0;

    private Script script = null;

    private boolean isRunning;

    // Do NOT write to this variable but use update...() method
    private SPDocument registerSPDoc;
    
    // Has to be stored between requests
    private Map<String, String> registerVariables = new HashMap<String, String>();

    private XPathResolver resolver = new XPathResolver();
    private PageAliasResolver pageAliasResolver;
    
    public void setPageAliasResolver(PageAliasResolver pageAliasResolver) {
        this.pageAliasResolver = pageAliasResolver;
    }
    
    public void setScript(Script script) {
        if (this.script != null) {
            throw new IllegalStateException("Script can only be set once per VM instance");
        }
        this.script = script;
        this.il     = script.getInstructions();
        this.ip     = 0;
    }

    public void loadVMState(VMState state) {
        this.script = state.getScript();
        this.il     = script.getInstructions();
        this.ip     = state.getIp();
        this.registerVariables.clear();
        this.registerVariables.putAll(state.getVariables());
    }

    public VMState saveVMState() {
        VMState state = new VMState();
        state.setScript(script);
        state.setIp(ip);
        state.setVariables(registerVariables);
        return state;
    }

    public SPDocument run(PfixServletRequest preq, SPDocument spdoc, ExtendedContext rcontext, Map<String, String> params) throws PustefixApplicationException, PustefixCoreException {
        isRunning = true;
        
        // Make sure resolver and registers are set up
        // with correct data
        resolver.setParams(params);
        resolver.setVariables(this.registerVariables);
        updateRegisterSPDoc(spdoc);

        // Check whether a script has been loaded
        if (script == null) {
            isRunning = false;
            throw new IllegalStateException("Script has to be loaded before running the machine");
        }
        
        mainloop: while (ip < il.length) {
            Instruction instr = il[ip];
            
            if (instr instanceof ExitInstruction) {
                ExitInstruction in = (ExitInstruction) instr;
                Map<String, String[]> formparams = new HashMap<String, String[]>();
                for (String key : in.getParams().keySet()) {
                    List<ParamValueObject> pvolist = in.getParams().get(key);
                    String[] rlist = new String[pvolist.size()];
                    for (int i = 0; i < pvolist.size(); i++) {
                        rlist[i] = pvolist.get(i).resolve(resolver);
                    }
                    formparams.put(key, rlist);
                }

                isRunning = false;
                return mangleDoc(registerSPDoc, formparams, false, false);
                
            } else if (instr instanceof InteractiveRequestInstruction) {
                InteractiveRequestInstruction in = (InteractiveRequestInstruction) instr;
                if (registerSPDoc == null) {
                    try {
                        updateRegisterSPDoc(rcontext.handleRequest(preq));
                    } finally {
                        // Make sure scripted flow is canceled on error
                        isRunning = false;
                    }
                }            
                ip++;

                Map<String, String[]> formparams = new HashMap<String, String[]>();
                for (String key : in.getParams().keySet()) {
                    List<ParamValueObject> pvolist = in.getParams().get(key);
                    String[] rlist = new String[pvolist.size()];
                    for (int i = 0; i < pvolist.size(); i++) {
                        rlist[i] = pvolist.get(i).resolve(resolver);
                    }
                    formparams.put(key, rlist);
                }
                
                isRunning = true;
                return mangleDoc(registerSPDoc, formparams, false, false);
                
            } else if (instr instanceof SetVariableInstruction) {
                SetVariableInstruction in = (SetVariableInstruction) instr;
                String varName = in.getVariableName();
                String varValue = in.getVariableValue().resolve(resolver);
                registerVariables.put(varName, varValue);
                ip++;
                
            } else if (instr instanceof JumpCondFalseInstruction) {
                JumpCondFalseInstruction in = (JumpCondFalseInstruction) instr;
                if (resolver.evalXPathBoolean(in.getCondition())) {
                    ip++;
                    continue;
                } else {
                    Instruction target = in.getTargetInstruction();
                    for (int i = 0; i < il.length; i++) {
                        if (il[i] == target) {
                            ip = i;
                            continue mainloop;
                        }
                    }
                    isRunning = false;
                    throw new RuntimeException("Jump references non-existing target!");
                }
                
            } else if (instr instanceof JumpUncondInstruction) {
                JumpUncondInstruction in = (JumpUncondInstruction) instr;
                Instruction target = in.getTargetInstruction();
                for (int i = 0; i < il.length; i++) {
                    if (il[i] == target) {
                        ip = i;
                        continue mainloop;
                    }
                }
                isRunning = false;
                throw new RuntimeException("Jump references non-existing target!");
                
            } else if (instr instanceof NopInstruction) {
                ip++;
                continue;

            } else if (instr instanceof VirtualRequestInstruction) {
                VirtualRequestInstruction in = (VirtualRequestInstruction) instr;
                Map<String, String[]> reqParams = new HashMap<String, String[]>();
                for (String key : in.getParams().keySet()) {
                    List<ParamValueObject> pvolist = in.getParams().get(key);
                    String[] rlist = new String[pvolist.size()];
                    for (int i = 0; i < pvolist.size(); i++) {
                        rlist[i] = pvolist.get(i).resolve(resolver);
                    }
                    reqParams.put(key, rlist);
                }
                boolean dointeractive = in.getDoInteractive();
                boolean reuseparams = in.getReuseParamsForInteractive(); 
                boolean retval = false;
                try {
                    retval = doVirtualRequest(in.getPagename(), reqParams, preq, rcontext);
                } finally {
                    // Make sure scripted flow is canceled on error
                    isRunning = false;
                }
                isRunning = true;
                ip++;

                if (!retval && dointeractive) {
                    if (reuseparams) {
                        return mangleDoc(registerSPDoc, reqParams, true, true);
                    } else {
                        return mangleDoc(registerSPDoc, null, true, true);
                    }
                }
                
                continue;
            } else {
                isRunning = false;
                throw new RuntimeException("Found instruction not understood by the VM!");
            }
        }

        // Reached end of script
        isRunning = false;
        return mangleDoc(registerSPDoc, null, false, false);
    }

    public boolean isExitState() {
        return !isRunning;
    }

    private SPDocument mangleDoc(SPDocument spdoc, Map<String, String[]> formparams, boolean removeerrors, boolean removevalues) {
        Document doc = spdoc.getDocument();
        if (doc != null) {
            Element root = doc.getDocumentElement();
            root.setAttribute("scriptedflowname", script.getName());
            root.setAttribute("scriptedflowrunning", "" + isRunning);

            if (removevalues) {
                Element formvalues = (Element) resolver.evalXPathNode("/formresult/formvalues");
                Node nextNode;
                while ((nextNode = formvalues.getFirstChild()) != null) {
                    formvalues.removeChild(nextNode);
                }
            }
            if (removeerrors) {
                Element formerrors = (Element) resolver.evalXPathNode("/formresult/formerrors");
                Node nextNode;
                while ((nextNode = formerrors.getFirstChild()) != null) {
                    formerrors.removeChild(nextNode);
                }
            }
            if (formparams != null) {
                Element formvalues = (Element) resolver.evalXPathNode("/formresult/formvalues");
                for (String pname : formparams.keySet()) {
                    String[] values = formparams.get(pname);
                    if (values != null && values.length > 0) {
                        for (String value : values) {
                            Element param = doc.createElement("param");
                            param.setAttribute("name", pname);
                            param.appendChild(doc.createTextNode(value));
                            formvalues.appendChild(param);
                        }
                    }
                }
            }
        }
        return spdoc;
    }
    
    private boolean doVirtualRequest(String pagename, Map<String, String[]> reqParams, PfixServletRequest origPreq, ExtendedContext rcontext) throws PustefixApplicationException, PustefixCoreException {

        HttpServletRequest vhttpreq = new VirtualHttpServletRequest(origPreq.getRequest(), pagename, reqParams);
        PfixServletRequest vpreq    = new PfixServletRequestImpl(vhttpreq, System.getProperties(), pageAliasResolver);
        
        // Send request to the context and use returned SPDocument
        // for further processing
        SPDocument newdoc = rcontext.handleRequest(vpreq);
        if (newdoc != null) {
            updateRegisterSPDoc(newdoc);
        }
        
        if (resolver.evalXPathBoolean("/formresult/formerrors/error")) {
            return false;
        } else {
            return true;
        }
    }

    private void updateRegisterSPDoc(SPDocument spdoc) {
        this.registerSPDoc = spdoc;
        this.resolver.setSPDocument(spdoc);
    }
}
