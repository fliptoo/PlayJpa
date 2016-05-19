import java.io.File

import com.fliptoo.playjpa.PlayJpa
import sbt.Keys._
import sbt._
import sbt.inc._
import sbt.plugins.JvmPlugin

object Imports {
  val jpaEnhancer = taskKey[Compiler.CompileResult => Compiler.CompileResult]("Create the function that will enhance JPA entities")
}

object Plugin extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = allRequirements

  val autoImport = Imports

  import Imports._

  override def projectSettings = Seq(
  ) ++ inConfig(Compile)(scopedSettings) ++ inConfig(Test)(scopedSettings)

  private def scopedSettings: Seq[Setting[_]] = Seq(
    sources in jpaEnhancer := unmanagedSources.value.filter(_.getName.endsWith(".java")),
    manipulateBytecode := {
      jpaEnhancer.value(manipulateBytecode.value)
    },
    jpaEnhancer <<= bytecodeEnhance(jpaEnhancer,  (PlayJpa.scan _).curried)
  )

  private def bytecodeEnhance(task: TaskKey[_], generateTask: String => File => Boolean): Def.Initialize[Task[Compiler.CompileResult => Compiler.CompileResult]] = Def.task {
    { result =>
      val analysis = result.analysis
      val deps: Classpath = dependencyClasspath.value
      val classes: File = classDirectory.value
      val classpath = (deps.map(_.data.getAbsolutePath).toArray :+ classes.getAbsolutePath).mkString(java.io.File.pathSeparator)
      val extra = if (crossPaths.value) s"_${scalaBinaryVersion.value}" else ""
      val timestampFile = streams.value.cacheDirectory / s"play_instrumentation$extra"
      val lastEnhanced = if (timestampFile.exists) IO.read(timestampFile).toLong else Long.MinValue

      def getClassesForSources(sources: Seq[File]) = {
        sources.flatMap { source =>
          // Custom Enhancer must have a suffix of _Enhancer.java so that it will be pick up everytime
          if (source.getName.endsWith("_Enhancer.java") || analysis.apis.internal(source).compilation.startTime > lastEnhanced) {
            analysis.relations.products(source)
          } else {
            Nil
          }
        }
      }

      val classesToEnhance = getClassesForSources((sources in task).value)
      val enhancedClasses = classesToEnhance.filter(generateTask(classpath))
      PlayJpa.enhance()
      IO.write(timestampFile, System.currentTimeMillis.toString)

      if (enhancedClasses.nonEmpty) {
        /**
          * Updates stamp of product (class file) by preserving the type of a passed stamp.
          * This way any stamp incremental compiler chooses to use to mark class files will
          * be supported.
          */
        def updateStampForClassFile(classFile: File, stamp: Stamp): Stamp = stamp match {
          case _: Exists => Stamp.exists(classFile)
          case _: LastModified => Stamp.lastModified(classFile)
          case _: Hash => Stamp.hash(classFile)
        }
        // Since we may have modified some of the products of the incremental compiler, that is, the compiled template
        // classes and compiled Java sources, we need to update their timestamps in the incremental compiler, otherwise
        // the incremental compiler will see that they've changed since it last compiled them, and recompile them.
        val updatedAnalysis = analysis.copy(stamps = enhancedClasses.foldLeft(analysis.stamps) {
          (stamps, classFile) =>
            val existingStamp = stamps.product(classFile)
            if (existingStamp == Stamp.notPresent) {
              throw new java.io.IOException("Tried to update a stamp for class file that is not recorded as "
                + s"product of incremental compiler: $classFile")
            }
            stamps.markProduct(classFile, updateStampForClassFile(classFile, existingStamp))
        })
        result.copy(analysis = updatedAnalysis, hasModified = true)
      } else {
        result
      }
    }
  }
}
