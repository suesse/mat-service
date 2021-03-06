package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*

import java.util.zip.ZipFile

import javax.annotation.Resource

import org.junit.Ignore
import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class MatZipLoaderTestIT extends AbstractTestBase {

	@Resource
	def MatZipLoader loader
	
	@Resource
	def ValueSetRepository repo
	
	@Resource
	def ValueSetVersionRepository vrepo

	@Test
	void TestSetUp() {
		assertNotNull loader
	}
	
	@Test
	void TestLoad() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/test.zip"))
		
		loader.loadMatZip(zip)
		
		assertTrue repo.count() > 10
	}
	
	@Test
	void TestLoadCombined() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/combined.zip"))
		
		loader.loadCombinedMatZip(zip)
		
		assertTrue repo.count() > 10
	}
	
	@Test
	@Transactional
	void TestLoadNQF_0002() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/NQF_0002_HHS_Updated_Dec_2011.zip"))
		
		loader.loadMatZip(zip)
		
		def valueSet = repo.findOne("2.16.840.1.113883.3.464.0001.372")
		
		assertNotNull valueSet
		assertNotNull valueSet.currentVersion()
		
		def entries = vrepo.findValueSetEntriesByChangeSetUri(valueSet.currentVersion().changeSetUri, new PageRequest(0, 1000))
		entries.each {
			assertTrue it.codeSystem + " - " + it.code, it.code.length() > 1
		}

	}

    @Test
    @Transactional
    void TestRowToValueSetEntry(){
        def zip = new ZipFile(new File("src/test/resources/exampleMatZips/NQF_0002_HHS_Updated_Dec_2011.zip"))
        loader.loadMatZip(zip)

        def valueSet = repo.findOne("2.16.840.1.113883.3.464.0001.45")
        assertNotNull valueSet

				def code = 99281
        for (it in valueSet.currentVersion().entries()) {
            assertEquals code.toString(), it.code
						code++
        }
    }
}
