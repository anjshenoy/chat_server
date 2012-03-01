require 'json'

class Message
  SIGNING_IN = "signed in"
  SIGNING_OFF = "bye bye!"

  attr_accessor :header, :body, :time
  def initialize(from, text="")
    self.header = {"from" => from, "time" => Time.now}
    self.body = text
  end

  def self.signin_message(from)
    Message.new(from, SIGNING_IN)
  end

  def self.signoff_message(from)
    Message.new(from, SIGNING_OFF)
  end

  def say(text="")
    self.body = text
  end

  def to_json
    JSON.dump(self.to_hash)
  end

  def to_hash
    {"header" => self.header, "body" => self.body}
  end

  def self.try_convert(hash)
    if hash.instance_of?(Hash)
      if(hash.has_key?("header") && hash.has_key?("body") && hash["header"].has_key?("from") && hash["header"].has_key?("time"))
        m = Message.new(nil, hash["body"])
        m.header = hash["header"]
        return m
      end
    else
      raise ArgumentError.new("Must be a hash to convert")
    end
  end

  def from
    self.header["from"]
  end

  def time_sent
    self.header["time"]
  end

  def print
    "#{self.from}: #{self.body}"
  end

  def signing_in?
    self.body == SIGNING_IN
  end

  def signing_off?
    self.body == SIGNING_OFF
  end
end
