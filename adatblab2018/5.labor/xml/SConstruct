# SCons build descriptor for the Databases laboratory XML/XSL exercise
# Jozsef Marton, 2017.

import os.path

# Saxon library path relative to the project root directory (# stands for "relative to the project root")
saxon_jar_path=File('#lib/saxon9he.jar')

vars = Variables(None, ARGUMENTS)
vars.Add('DS', 'Pass a single data source XML file basename (i.e. without extension) '
               'as input for the transformation. '
               'E.g. pass SHIP to use SHIP.xml (which, in fact is the default).'
             , 'SHIP')
env = Environment(variables = vars)
Help(vars.GenerateHelpText(env))

# XSLT builder
xslt_build = Builder(action="java -cp $SAXONLIB net.sf.saxon.Transform -s:${SOURCES[0]} -xsl:${SOURCES[1]} -o:${TARGETS[0]}")
xslt_env = Environment(variables = vars, SAXONLIB=saxon_jar_path)
xslt_env.Append(BUILDERS={'XSLT': xslt_build})
for task in [
             {"name": "list", "output_extension": "html", "extra_sources": []},
             {"name": "filter", "output_extension": "xml", "extra_sources": []},
             {"name": "index", "output_extension": "html", "extra_sources": ['src/list.xsl']},
             {"name": "join", "output_extension": "xml", "extra_sources": []},
             {"name": "join2", "output_extension": "xml", "extra_sources": ['src/join.xsl']},
             {"name": "list2csv", "output_extension": "csv", "extra_sources": []},
            ]:
    main_source_file = 'src/' + task["name"] + '.xsl'
    if os.path.isfile(main_source_file):
        xslt_env.XSLT(target = 'web/' + task["name"] + '.' + task["output_extension"]
                    , source = xslt_env.subst(['${DS}.xml', main_source_file] + task["extra_sources"]))

# Create lab5xml.zip, the file to submit
# Do not include neither lib/saxon9he.jar nor other files under lib/
release_env = Environment(variables = vars)
release_env.Zip(target='lab5xml.zip',
                source=['src/', 'web/', '${DS}.xml'],
               )
