package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.model.core.SourceReference
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.RoleReference
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import org.apache.commons.lang.StringUtils

object MatValueSetUtils {

  def buildValueSetReference(valueSetVersion: ValueSetVersion, urlConstructor: UrlConstructor): ValueSetReference = {
    buildValueSetReference(valueSetVersion.valueSet, urlConstructor)
  }

  def buildValueSetReference(valueSet: ValueSet, urlConstructor: UrlConstructor): ValueSetReference = {
    val ref = new ValueSetReference()
    ref.setContent(valueSet.name)
    ref.setUri(valueSet.uri)
    ref.setHref(urlConstructor.createValueSetUrl(valueSet.name))

    ref
  }
  
   def buildValueSetDefinitionReference(
    valueSetVersion: ValueSetVersion,
    urlConstructor: UrlConstructor): ValueSetDefinitionReference = {
     buildValueSetDefinitionReference(
         valueSetVersion.valueSet.name,
         valueSetVersion.valueSet.uri,
         valueSetVersion,
         urlConstructor)
   }

  def buildValueSetDefinitionReference(
    name: String, about: String,
    valueSetVersion: ValueSetVersion,
    urlConstructor: UrlConstructor): ValueSetDefinitionReference = {

    val valueSetDefName = getValueSetDefName(valueSetVersion)

    buildValueSetDefinitionReference(
      name, about,
      valueSetDefName, valueSetVersion.documentUri,
      urlConstructor)
  }
  
  def getValueSetDefName(valueSetVersion: ValueSetVersion) = {
      val valueSetDefName =
      if (StringUtils.isNotBlank(valueSetVersion.version)) {
        valueSetVersion.version
      } else {
        valueSetVersion.documentUri
      }
      
      valueSetDefName
  }

  def buildValueSetDefinitionReference(
    name: String, about: String,
    defName: String, defDocUri: String,
    urlConstructor: UrlConstructor) = {
    val currentDefinition = new ValueSetDefinitionReference()

    val valueSetRef = new ValueSetReference(name)
    valueSetRef.setUri(about)
    valueSetRef.setHref(urlConstructor.createValueSetUrl(name))
    currentDefinition.setValueSet(valueSetRef)

    val valueSetDefRef = new NameAndMeaningReference(defName)
    valueSetDefRef.setUri(defDocUri)
    valueSetDefRef.setHref(urlConstructor.createValueSetDefinitionUrl(name, defName))
    currentDefinition.setValueSetDefinition(valueSetDefRef)

    currentDefinition
  }: ValueSetDefinitionReference

  def sourceAndRole = {
    val sourceAndRoleRef = new SourceAndRoleReference()
    sourceAndRoleRef.setRole(creatorRole)

    val sourceRef = new SourceReference("National Committee for Quality Assurance")
    sourceAndRoleRef.setSource(sourceRef)

    sourceAndRoleRef
  }

  def creatorRole = {
    val role = new RoleReference()
    role.setContent("creator")
    role.setUri("http://purl.org/dc/elements/1.1/creator")
    role
  }
  
  def getIncludedVersionIds(version: ValueSetVersion, repos: ValueSetRepository):Seq[String] = {
    val includedOids = version.includesValueSets
    
    includedOids.foldLeft(Seq(version.documentUri))(
        (seq, oid) => {
          seq ++ getIncludedVersionIds( repos.findOne( oid ).currentVersion, repos )
        }
    )
  }
}