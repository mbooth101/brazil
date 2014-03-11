port = 1701
log =5
# Config file for Brazil ReXXScripting Test

handler=sunlabs.brazil.server.ChainHandler
handlers=calc file template

# turn on the calculator as a side effect of name lookups

calc.class=sunlabs.brazil.properties.ExprPropsHandler

# all files in the "data" directory are delivered "as is" with
# no processing

file.class=sunlabs.brazil.server.FileHandler
file.root=data
mime.au=audio/basic
mime.gif=image/gif

# Normal template processing, using either of the two user sessions

template.class=sunlabs.brazil.template.TemplateHandler
template.templates=bsl rexx include set headers form date eval subst debug

# templates and their options

bsl.class=sunlabs.brazil.template.BSLTemplate
rexx.class=sunlabs.brazil.rexxshell.ReXXShell
include.class=sunlabs.brazil.template.IncludeTemplate
set.class=sunlabs.brazil.template.SetTemplate
set.query=query.
set.headers=headers.
set.saveOk=true
queue.class=sunlabs.brazil.template.QueueTemplate
headers.class=sunlabs.brazil.template.AddHeaderTemplate
form.class=sunlabs.brazil.template.FormTemplate
date.class=sunlabs.brazil.sunlabs.DateTemplate
eval.class=sunlabs.brazil.template.ScriptEvalTemplate
digest.class=sunlabs.brazil.sunlabs.DigestTemplate
subst.class=sunlabs.brazil.sunlabs.SubstAllTemplate
dir.class=sunlabs.brazil.beanshell.BeanShellServerTemplate
dir.script=test.bsh
debug.class=sunlabs.brazil.template.DebugTemplate
tcl.class=sunlabs.brazil.tcl.TclServerTemplate
debug.debug=1
