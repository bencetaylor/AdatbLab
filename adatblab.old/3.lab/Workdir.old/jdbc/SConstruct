# Path of JDK's bin directory
# jdk_bin_path = ""
jdk_bin_path = "/usr/java/default/bin/"
jdbc_driver_jarname = 'ojdbc7.jar'

# Compile .java files, generate .class files
javac_env = Environment(JAVACFLAGS='-encoding UTF-8', JAVACLASSPATH=['.'] + Glob(pattern='lib/*.jar', strings=True))
my_class_files = javac_env.Java('classes', 'src')

# Generate an unsigned .jar file from .class files
jar_env = Environment()
#jar_env.Jar(target='unsigned.jar', source=my_class_files+['conf/MANIFEST.MF', 'resources/'] )
# jmarton, 20150414:
# Scons 2.3.4, 2.4.1 sometimes fails to recognize a few Java inner class files generated
# (see classes/ after "scons -c").
#
# jmarton, 20160321:
# So we add classes dir explicitly to the sources and set .attributes.java_classdir,
# that will, in turn, set appropriate -C flag for the jar command.
#
# This should not break build-dependency handling as inner class files
# are always re-generated when the Java source file is being re-compiled.
# If in doubt, run "scons -c" first, and after backing up classes/ dir, "rm -rf classes/"
#
# jmarton, 20160324
# add resources explicitly by globbing.
# Note: Jar builder of SCons should probably have the option
# source_scanner = SCons.Defaults.DirScanner
# to allow for resource directory inclusion.
my_classes_dir = jar_env.Dir('classes')
my_classes_dir.attributes.java_classdir = jar_env.Dir('classes')
jar_env.Jar(target='unsigned.jar',
            source=[my_classes_dir, 'conf/MANIFEST.MF'] + Glob('resources/*'),
           )

# Sign .jar file
# Create builder (jarsigner)
jarsigner_build = Builder(action=jdk_bin_path+'jarsigner -keystore $KEYSTORE '
                          '-storepass webstart -tsa $TSA -signedjar $TARGET $SOURCE webstart')
# Set parameters
jarsigner_env = Environment(BUILDERS={'JarSigner': jarsigner_build},
                            KEYSTORE='conf/webstart.keystore',
                            TSA='http://timestamp.digicert.com/',
                           )
# Sign .jar file
jar_file = jarsigner_env.JarSigner(target='MySignedApplication.jar', source='unsigned.jar')
ojdbc_jar_file = jarsigner_env.JarSigner(target='web/' + jdbc_driver_jarname, source='lib/' + jdbc_driver_jarname)

# Deploy .jar file and JDBC driver into 'web' directory
jardeploy_env = Environment()
jardeploy_env.Install(target='web/', source=jar_file)

# Create lab5jdbc.zip, the file to submit
# Exclude {web,lib}/ojdbc7.jar.
# Note: Glob() in SCons 2.4.1 supports exclude that is being worked around for 2.3.x
release_env = Environment()
release_env.Zip(target='lab5jdbc.zip',
                source=['src/', 'resources/',
                        [x for x in Glob('web/*', strings=True) + Glob('lib/*', strings=True)
                         if not x.endswith('/' + jdbc_driver_jarname)
                        ],
                       ],
               )
