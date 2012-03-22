require 'socket'
require 'base64'
require './message'

class Client
  attr_accessor :name

  def initialize(name)
    self.name = name
    @server = TCPSocket.new('localhost', 39090)
    Thread.start do 
      until((x = @server.gets).nil?)
        puts Message.try_convert(JSON.load(x)).print
      end
    end
  end

  def start
    puts "here we go"
    @server.puts(Message.signin_message(self.name).to_json)
    until((x = gets) == "bye\n") do
      m = Message.new(self.name, x)
      @server.puts(m.to_json)
    end
    @server.puts(Message.signoff_message(self.name).to_json)
  end

end

#client = Client.new("test")
#client.start
