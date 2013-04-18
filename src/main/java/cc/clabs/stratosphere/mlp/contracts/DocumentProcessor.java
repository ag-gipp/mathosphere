/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package cc.clabs.stratosphere.mlp.contracts;

import cc.clabs.stratosphere.mlp.types.PactIdentifiers;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class DocumentProcessor extends MapStub {
        
    private static final Log LOG = LogFactory.getLog( DocumentProcessor.class );
    
    private final PactString plaintext = new PactString();
    private final PactIdentifiers list = new PactIdentifiers();
    private final PactInteger id = new PactInteger();
    private final PactRecord target = new PactRecord();
   
    @Override
    public void map( PactRecord record, Collector<PactRecord> collector ) {
        
        WikiDocument doc = (WikiDocument) record.getField( 0, WikiDocument.class );
        
        // populate the list of known identifiers
        list.clear();
        for ( PactString var : doc.getKnownIdentifiers() )
            list.add( var );

        // generate a plaintext version of the document
        plaintext.setValue( doc.getPlainText() );
        
        LOG.info( "Analyzed Page '"+ doc.getTitle() +"' (id: "+ doc.getId() +"), found identifiers: " + list.toString() );
        
        // set the id
        id.setValue( doc.getId() );
                
        // finally emit all parts
        target.clear();
        target.setField( 0, id );
        target.setField( 1, plaintext );
        target.setField( 2, list );
        collector.collect( target );   
    }
}








