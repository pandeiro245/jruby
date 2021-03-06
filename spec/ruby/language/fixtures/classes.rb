module LanguageSpecs
  class ClassWith_to_s
    def initialize(to_s_callback = nil)
      @to_s_callback = to_s_callback
    end

    def to_s
      if @to_s_callback then
        @to_s_callback.call()
      else
        "class_with_to_s"
      end
    end
  end

  class ClassWithout_to_s
    undef :to_s
  end

  class BasicObjectClass < ::BasicObject
    def create_lambda
      -> { }
    end
  end

  #############################################################################
  # Regexp support
  #############################################################################

  def self.paired_delimiters
    [%w[( )], %w[{ }], %w[< >], ["[", "]"]]
  end

  def self.non_paired_delimiters
    %w[~ ! # $ % ^ & * _ + ` - = " ' , . ? / | \\]
  end

  def self.blanks
    " \t"
  end

  def self.white_spaces
    return blanks + "\f\n\r\v"
  end

  def self.non_alphanum_non_space
    '~!@#$%^&*()+-\|{}[]:";\'<>?,./'
  end

  def self.punctuations
    ",.?" # TODO - Need to fill in the full list
  end

  def self.get_regexp_with_substitution o
    /#{o}/o
  end
end
