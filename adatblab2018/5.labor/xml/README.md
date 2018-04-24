# Databases Laboratory XML/XSLT demo application

## Usage

 1. Replace `SHIP.xml` with your actual data source
 2. Install dependencies
 3. Modify XSLT files under `src/`
 4. Run `scons DS=YOUR_DS` and enjoy!  
    Replace YOUR_DS with the name of your data source name without extension, e.g. `SHIP` for `SHIP.xml`.!

## Dependencies

 1. SAXON - The XSLT and XQuery Processor, home edition, Java, http://saxon.sourceforge.net/  
    place `saxon9he.jar` under `lib/`
 2. SCons: A software construction tool http://scons.org/
