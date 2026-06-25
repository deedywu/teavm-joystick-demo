cmake_minimum_required(VERSION 3.10)
project(app C)

set(CMAKE_C_STANDARD 11)

# Output directory configuration
if(CMAKE_CONFIGURATION_TYPES)
    foreach(config ${CMAKE_CONFIGURATION_TYPES})
        string(TOUPPER ${config} config_upper)
        set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${config_upper} "@APP_OUTPUT_DIR@")
    endforeach()
else()
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "@APP_OUTPUT_DIR@")
endif()

# Default to Release build if not specified
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

if (UNIX)
  find_package(glfw3 CONFIG REQUIRED)
  find_package(OpenGL REQUIRED)
  find_package(GLEW REQUIRED)
endif()

if (WIN32)
  # Define GLEW_STATIC
  add_definitions(-DGLEW_STATIC)
endif()

if (WIN32)
# Include directories - using CMAKE_CURRENT_SOURCE_DIR
  include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/glfw/include")
  include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/glew-2.3.0/include")
endif()

include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/etc1")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/gdx")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_optimizations/pure")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_optimizations/teavm")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_stats")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/stb/include")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/src/freetype/include")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/src/freetype/include/freetype")
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/thirdparty/freetype/include")

if (WIN32)
  # Link directories
  link_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/glfw/lib-vc2022")
  link_directories("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/glew-2.3.0/lib/Release/x64")
endif()

# Source files
set(SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/c/src/app_include.c")

# Collect external C sources from gdx
file(GLOB_RECURSE EXTERNAL_SOURCES_0 "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/gdx/*.c")
list(APPEND SOURCES ${EXTERNAL_SOURCES_0})

# Collect shared TeaVM native diagnostic C sources
set(TEAVM_MEMORY_STATS_SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_stats/teavm_memory_stats.c")
if(EXISTS "${TEAVM_MEMORY_STATS_SOURCE}")
  list(APPEND SOURCES "${TEAVM_MEMORY_STATS_SOURCE}")
endif()

# Collect shared TeaVM optimization C sources
list(APPEND SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_optimizations/teavm/teavm_fastmath.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_optimizations/teavm/teavm_matrix4.c"
)
set(TEAVM_GENERATED_SOURCES_LIST "${CMAKE_CURRENT_SOURCE_DIR}/c/src/all.txt")
set(TEAVM_SPRITEBATCH_SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/teavm_optimizations/teavm/teavm_spritebatch.c")
set(TEAVM_SPRITEBATCH_LONG_SOURCE_PATH "classes/com/badlogic/gdx/graphics/g2d/SpriteBatch.c")
set(TEAVM_SPRITEBATCH_SHORT_SOURCE_PATH "c/c/b/g/g/g/SpriteBatch.c")
set(TEAVM_SPRITEBATCH_LONG_HEADER "${CMAKE_CURRENT_SOURCE_DIR}/c/src/classes/com/badlogic/gdx/graphics/g2d/SpriteBatch.h")
set(TEAVM_SPRITEBATCH_SHORT_HEADER "${CMAKE_CURRENT_SOURCE_DIR}/c/src/c/c/b/g/g/g/SpriteBatch.h")
set(TEAVM_SPRITEBATCH_GENERATED FALSE)
set(TEAVM_SPRITEBATCH_SHORT_HEADERS FALSE)
if(EXISTS "${TEAVM_GENERATED_SOURCES_LIST}")
  file(STRINGS "${TEAVM_GENERATED_SOURCES_LIST}" TEAVM_GENERATED_SOURCES)
  list(FIND TEAVM_GENERATED_SOURCES "${TEAVM_SPRITEBATCH_SHORT_SOURCE_PATH}" TEAVM_SPRITEBATCH_SHORT_INDEX)
  list(FIND TEAVM_GENERATED_SOURCES "${TEAVM_SPRITEBATCH_LONG_SOURCE_PATH}" TEAVM_SPRITEBATCH_LONG_INDEX)
  if(NOT TEAVM_SPRITEBATCH_SHORT_INDEX EQUAL -1)
    set(TEAVM_SPRITEBATCH_GENERATED TRUE)
    set(TEAVM_SPRITEBATCH_SHORT_HEADERS TRUE)
  elseif(NOT TEAVM_SPRITEBATCH_LONG_INDEX EQUAL -1)
    set(TEAVM_SPRITEBATCH_GENERATED TRUE)
  endif()
endif()
if(NOT TEAVM_SPRITEBATCH_GENERATED)
  if(EXISTS "${TEAVM_SPRITEBATCH_SHORT_HEADER}")
    set(TEAVM_SPRITEBATCH_GENERATED TRUE)
    set(TEAVM_SPRITEBATCH_SHORT_HEADERS TRUE)
  elseif(EXISTS "${TEAVM_SPRITEBATCH_LONG_HEADER}")
    set(TEAVM_SPRITEBATCH_GENERATED TRUE)
  endif()
endif()
if(EXISTS "${TEAVM_SPRITEBATCH_SOURCE}" AND TEAVM_SPRITEBATCH_GENERATED)
  list(APPEND SOURCES "${TEAVM_SPRITEBATCH_SOURCE}")
endif()

set(FT_DISABLE_ZLIB ON CACHE BOOL "" FORCE)
set(FT_DISABLE_BZIP2 ON CACHE BOOL "" FORCE)
set(FT_DISABLE_PNG ON CACHE BOOL "" FORCE)
set(FT_DISABLE_BROTLI ON CACHE BOOL "" FORCE)
set(FT_DISABLE_HARFBUZZ ON CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/thirdparty/freetype"
                 "${CMAKE_CURRENT_BINARY_DIR}/freetype-build"
                 EXCLUDE_FROM_ALL)

# Collect freetype bridge sources
file(GLOB_RECURSE EXTERNAL_SOURCES_FREETYPE "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/src/freetype/src/*.c")
list(APPEND SOURCES ${EXTERNAL_SOURCES_FREETYPE})

# Collect external C++ sources from etc1
file(GLOB_RECURSE EXTERNAL_SOURCES_2 "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/etc1/etc1/*.cpp")
list(APPEND SOURCES ${EXTERNAL_SOURCES_2})

# Add executable
add_executable(app ${SOURCES})

if(TEAVM_SPRITEBATCH_SHORT_HEADERS)
  target_compile_definitions(app PRIVATE TEAVM_GENERATED_SHORT_FILE_NAMES=1)
endif()

set(GDX2D_FREETYPE_BRIDGE_HEADER "${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/src/freetype/include/gdx2d_freetype_bridge.h")

if (UNIX AND CMAKE_C_COMPILER_ID MATCHES "GNU|Clang" AND TARGET freetype)
  target_compile_definitions(freetype PRIVATE _DEFAULT_SOURCE HAVE_UNISTD_H HAVE_FCNTL_H)
endif()

if (MSVC)
  target_compile_options(app PRIVATE /FI"${GDX2D_FREETYPE_BRIDGE_HEADER}")
  if (TARGET freetype)
    target_compile_options(freetype PRIVATE /FI"${GDX2D_FREETYPE_BRIDGE_HEADER}")
  endif()
elseif (CMAKE_C_COMPILER_ID MATCHES "GNU|Clang")
  target_compile_options(app PRIVATE -include "${GDX2D_FREETYPE_BRIDGE_HEADER}")
  if (TARGET freetype)
    target_compile_options(freetype PRIVATE -include "${GDX2D_FREETYPE_BRIDGE_HEADER}")
  endif()
endif()

if (MSVC)
  target_compile_options(app PRIVATE
      $<$<CONFIG:Debug>:/Od /Zi>
      $<$<CONFIG:Release>:/O2 /Ob2 /Oi /Ot>)
elseif (CMAKE_C_COMPILER_ID MATCHES "GNU|Clang")
  target_compile_options(app PRIVATE
      $<$<CONFIG:Debug>:-g>
      $<$<CONFIG:Release>:-O3>)
endif()

# Set output name based on build configuration
set_target_properties(app PROPERTIES
    OUTPUT_NAME "app_$<IF:$<CONFIG:Debug>,debug,release>")

if (WIN32)
  # Set Visual Studio debugger working directory
  set_target_properties(app PROPERTIES
      VS_DEBUGGER_WORKING_DIRECTORY "@APP_OUTPUT_DIR@")
  # Link libraries
  target_link_libraries(app glfw3 opengl32 glew32s freetype)

  # Set startup project for Visual Studio
  set_property(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} PROPERTY VS_STARTUP_PROJECT app)
endif()
if (UNIX)
  target_link_libraries(app PRIVATE OpenGL::GL glfw GLEW::GLEW freetype m)
endif()

set_target_properties(app PROPERTIES
    INTERPROCEDURAL_OPTIMIZATION_RELEASE ON
)
